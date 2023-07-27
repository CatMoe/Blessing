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

import catmoe.fallencrystal.moefilter.network.limbo.packet.handshake.Protocol
import catmoe.fallencrystal.moefilter.network.limbo.util.Version
import catmoe.fallencrystal.moefilter.util.message.v2.MessageUtil
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageDecoder

@Suppress("MemberVisibilityCanBePrivate")
class LimboDecoder(var version: Version?) : MessageToMessageDecoder<ByteBuf>() {

    var mappings = Protocol.HANDSHAKING.serverBound.registry[version ?: Version.min]

    fun switchVersion(version: Version, state: Protocol) {
        this.version=version
        mappings = state.serverBound.registry[version]
        MessageUtil.logInfo("[MoeLimbo] Decoder mappings refreshed. Now switch to state ${state.name} for version ${version.name}")
    }

    override fun decode(ctx: ChannelHandlerContext, byteBuf: ByteBuf, out: MutableList<Any>?) {
        try {
            if (!ctx.channel().isActive) return
            if (mappings == null) throw NullPointerException("Mappings cannot be null!")
            val byteMessage = catmoe.fallencrystal.moefilter.network.limbo.packet.ByteMessage(byteBuf)
            val id = byteMessage.readVarInt()
            val packet = mappings!!.getPacket(id)
            if (packet == null) { MessageUtil.logWarn("[MoeLimbo] Cancelled unsupported or invalid packet ${"0x%02X".format(id)} with ${byteBuf.readableBytes()} bytes length"); return }
            MessageUtil.logInfo("[MoeLimbo] Decoding packet ${"0x%02X".format(id)} (${packet::class.java.simpleName}) for version ${(version ?: Version.UNDEFINED).name} with ${byteBuf.readableBytes()} bytes length")
            try {
                val version = if (this.version == null || this.version == Version.UNDEFINED) Version.V1_7_2 else this.version
                packet.decode(byteMessage, ctx.channel(), version ?: Version.min)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
            ctx.fireChannelRead(packet)
        } catch (ex: NullPointerException) {
            ex.printStackTrace()
        }
    }
}