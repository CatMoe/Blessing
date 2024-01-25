/*
 * Copyright (C) 2023-2024. CatMoe / Blessing Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.miaomoe.blessing.fallback.handler

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageDecoder
import net.miaomoe.blessing.fallback.packet.ExplicitPacket
import net.miaomoe.blessing.protocol.mappings.ProtocolMappings
import net.miaomoe.blessing.protocol.packet.type.PacketToServer
import net.miaomoe.blessing.protocol.registry.State
import net.miaomoe.blessing.protocol.util.ByteMessage
import net.miaomoe.blessing.protocol.util.ByteMessage.Companion.toByteMessage
import net.miaomoe.blessing.protocol.version.Version
import org.jetbrains.kotlin.utils.addToStdlib.ifTrue

@Suppress("MemberVisibilityCanBePrivate")
class FallbackDecoder(
    var mappings: ProtocolMappings = State.HANDSHAKE.serverbound.value,
    var version: Version = Version.UNDEFINED,
    val throwWhenEmptyBuffer: Boolean = true,
    val checkRemainBytes: Boolean = false,
    val handler: FallbackHandler? = null
) : MessageToMessageDecoder<ByteBuf>() {

    override fun decode(ctx: ChannelHandlerContext, byteBuf: ByteBuf, list: MutableList<Any>) {
        val unreadReadable = byteBuf.readableBytes()
        handler?.debug { "[Decoder] Processing ByteBuf with $unreadReadable bytes." }
        (unreadReadable == 0).ifTrue {
            require(throwWhenEmptyBuffer) { "Null byte buffers are not acceptable!" }
            return
        }
        val byteMessage = ByteMessage(byteBuf)
        val id = byteMessage.readVarInt()
        val formatId = "0x%02X".format(id)
        handler?.debug { "[Decoder] Handling packet with id $formatId" }
        try {
            val packet = mappings.getMappings(version, id).init.get()
            require(packet is PacketToServer) { "Getting packet from mappings with id $formatId. But got a non PacketToServer packet. ($packet)" }
            packet.decode(byteMessage, version)
            byteBuf.readableBytes().let {
                val isZero = it == 0
                handler?.debug { "[Decoder] Decoded with $packet ${if (!isZero) "($it bytes remaining)" else "" }" }
                checkRemainBytes.ifTrue { require(isZero) { "checkRemainBytes is true. But found $it remaining bytes not decoded." } }
            }
            ctx.fireChannelRead(packet)
        } catch (exception: NullPointerException) {
            handler?.debug { "[Decoder] A NullPointerException has thrown when getting packet. (${exception.localizedMessage}) That will be converted to ExplicitPacket." }
            ctx.fireChannelRead(ExplicitPacket(id, byteBuf.toByteMessage().toByteArray(), "A packet with a unknown id."))
        }
    }

    override fun toString() = "FallbackDecoder(mappings=$mappings, version=${version.name}, throwWhenEmptyBuffer=$throwWhenEmptyBuffer, checkRemainBytes=$checkRemainBytes)"

}