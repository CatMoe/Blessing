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

import catmoe.fallencrystal.moefilter.network.limbo.handler.LimboHandler
import catmoe.fallencrystal.moefilter.network.limbo.listener.LimboListener
import catmoe.fallencrystal.moefilter.network.limbo.packet.protocol.Protocol
import catmoe.fallencrystal.moefilter.network.limbo.util.Version
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
    }

    override fun decode(ctx: ChannelHandlerContext, byteBuf: ByteBuf, out: MutableList<Any>?) {
        try {
            if (!ctx.channel().isActive) return
            if (mappings == null) throw NullPointerException("Mappings cannot be null!")
            val byteMessage = ByteMessage(byteBuf)
            val id = byteMessage.readVarInt()
            val packet = mappings!!.getPacket(id) ?: return
            try {
                val version = if (this.version == null || this.version == Version.UNDEFINED) Version.V1_7_2 else this.version
                packet.decode(byteMessage, ctx.channel(), version)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
            ctx.fireChannelRead(packet)
            LimboListener.handleReceived(packet, handler)
        } catch (ex: NullPointerException) {
            ex.printStackTrace()
        }
    }
}