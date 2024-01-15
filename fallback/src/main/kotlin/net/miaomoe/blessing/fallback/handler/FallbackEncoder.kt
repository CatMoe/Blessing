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
import io.netty.handler.codec.MessageToByteEncoder
import net.miaomoe.blessing.fallback.packet.ExplicitPacket
import net.miaomoe.blessing.fallback.packet.PacketCached
import net.miaomoe.blessing.protocol.mappings.ProtocolMappings
import net.miaomoe.blessing.protocol.packet.type.PacketToClient
import net.miaomoe.blessing.protocol.registry.State
import net.miaomoe.blessing.protocol.util.ByteMessage
import net.miaomoe.blessing.protocol.version.Version

@Suppress("MemberVisibilityCanBePrivate")
class FallbackEncoder(
    var mappings: ProtocolMappings = State.HANDSHAKE.clientbound.value,
    var version: Version = Version.UNDEFINED
) : MessageToByteEncoder<PacketToClient>() {

    override fun encode(ctx: ChannelHandlerContext, packet: PacketToClient, output: ByteBuf) {
        val byteBuf = ByteMessage(output)
        when (packet) {
            is PacketCached -> {
                byteBuf.writeVarInt(mappings.getId(version, packet.kClass))
                packet.byteArray?.let(byteBuf::writeBytes)
            }
            is ExplicitPacket -> {
                byteBuf.writeVarInt(packet.id)
                packet.byteArray?.let(byteBuf::writeBytes)
            }
            else -> {
                byteBuf.writeVarInt(mappings.getId(version, packet::class))
                packet.encode(byteBuf, version)
            }
        }
    }
}