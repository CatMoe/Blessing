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
import net.miaomoe.blessing.protocol.mappings.ProtocolMappings
import net.miaomoe.blessing.protocol.packet.type.PacketToServer
import net.miaomoe.blessing.protocol.registry.State
import net.miaomoe.blessing.protocol.util.ByteMessage
import net.miaomoe.blessing.protocol.version.Version

@Suppress("MemberVisibilityCanBePrivate")
class FallbackDecoder(
    var mappings: ProtocolMappings = State.HANDSHAKE.serverbound.value,
    var version: Version = Version.UNDEFINED
) : MessageToMessageDecoder<ByteBuf>() {

    override fun decode(ctx: ChannelHandlerContext, byteBuf: ByteBuf, p2: MutableList<Any>) {
        val byteMessage = ByteMessage(byteBuf)
        val id = byteMessage.readVarInt()
        val packet = mappings.getMappings(version, id).init.get()
        require(packet is PacketToServer) { "Getting packet from mappings with id ${"0x%02X".format(id)}. But decoder got a non PacketToServer packet." }
        packet.decode(byteMessage, version)
        ctx.fireChannelRead(packet)
    }

}