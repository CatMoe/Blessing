/*
 * Copyright 2023. CatMoe / FallenCrystal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package catmoe.fallencrystal.moefilter.network.limbo.netty

import catmoe.fallencrystal.moefilter.network.bungee.pipeline.MoeChannelHandler
import catmoe.fallencrystal.moefilter.network.common.ExceptionCatcher
import catmoe.fallencrystal.moefilter.network.limbo.handler.LimboHandler
import catmoe.fallencrystal.moefilter.network.limbo.handler.MoeLimbo
import catmoe.fallencrystal.moefilter.network.limbo.listener.LimboListener
import catmoe.fallencrystal.moefilter.network.limbo.packet.common.Unknown
import catmoe.fallencrystal.moefilter.network.limbo.packet.protocol.Protocol
import catmoe.fallencrystal.translation.utils.version.Version
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageDecoder

@Suppress("MemberVisibilityCanBePrivate")
class LimboDecoder(var version: Version?) : MessageToMessageDecoder<ByteBuf>() {

    var mappings = Protocol.HANDSHAKING.serverBound.registry[version ?: Version.min]
    var handler: LimboHandler? = null

    fun switchVersion(version: Version, state: Protocol) {
        this.version=version
        mappings = state.serverBound.registry[version]
        MoeLimbo.debug(handler,"Decoder state changed. Version: ${version.name} State: ${state.name}")
    }

    override fun decode(ctx: ChannelHandlerContext, byteBuf: ByteBuf, out: MutableList<Any>?) {
        if (!ctx.channel().isActive) return
        val byteMessage = ByteMessage(byteBuf)
        val id = byteMessage.readVarInt()
        // Unreached code? May just state == null?
        /*
        if (id == 0x00 && (handler?.state ?: Protocol.HANDSHAKING) == Protocol.HANDSHAKING) { MoeChannelHandler.sentHandshake.put(handler!!.channel, true) }
        else if (MoeChannelHandler.sentHandshake.getIfPresent(handler!!.channel) != true) throw InvalidPacketException("No valid handshake packet received")
         */
        MoeLimbo.debug(handler, "Decoding ${"0x%02X".format(id)} packet with ${byteMessage.readableBytes()} bytes length")
        val packet = mappings?.getPacket(id)
        if (packet == null) {
            LimboListener.handleReceived(Unknown(id), handler)
            if (mappings == null) {
                MoeLimbo.debug(handler,"Failed to decode incoming packet ${"0x%02X".format(id)}: Mappings is empty")
                throw NullPointerException("Mappings cannot be null!")
            }
            MoeLimbo.debug(handler,"Unknown incoming packet ${"0x%02X".format(id)}. Ignoring.")
            return
        }
        // Unreached code?
        val version = if (this.version == null || this.version == Version.UNDEFINED) Version.V1_7_6 else this.version
        // Try-catch 语句已被删除 因为对于某些异常解码的抛出可以直接顺着exception Caught方法直接切断连接并列入黑名单
        // 进行数据包调试时首选打开debug模式
        packet.decode(byteMessage, ctx.channel(), version)
        if (LimboListener.handleReceived(packet, handler)) return
        MoeLimbo.debug(handler, packet.toString())
        ctx.fireChannelRead(packet)
        MoeChannelHandler.sentHandshake.put(handler!!.channel, true)
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) { ExceptionCatcher.handle(ctx.channel(), cause) }
}