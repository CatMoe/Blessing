/*
 * Copyright (C) 2023-2023. CatMoe / MoeFilter Contributors
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

package catmoe.fallencrystal.moefilter.network.limbo.handler

import catmoe.fallencrystal.moefilter.network.common.ByteMessage
import catmoe.fallencrystal.moefilter.network.common.ExceptionCatcher
import catmoe.fallencrystal.moefilter.network.limbo.listener.LimboListener
import catmoe.fallencrystal.moefilter.network.limbo.packet.ExplicitPacket
import catmoe.fallencrystal.moefilter.network.limbo.packet.LimboPacket
import catmoe.fallencrystal.moefilter.network.limbo.packet.cache.PacketSnapshot
import catmoe.fallencrystal.moefilter.network.limbo.packet.protocol.Protocol
import catmoe.fallencrystal.translation.utils.version.Version
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder

class LimboEncoder(var version: Version?) : MessageToByteEncoder<LimboPacket>() {

    private var registry = Protocol.HANDSHAKING.clientBound.registry[version ?: Version.min]
    var handler: LimboHandler? = null

    fun switchVersion(version: Version, state: Protocol) {
        this.version=version
        registry = state.clientBound.registry[version]
        MoeLimbo.debug(handler, "Encoder state changed. Version: ${version.name} State: ${state.name}")
    }

    override fun encode(ctx: ChannelHandlerContext, packet: LimboPacket, out: ByteBuf) {
        val msg = ByteMessage(out)
        val packetId = when (packet) {
            is PacketSnapshot -> registry!!.getPacketId(packet.wrappedPacket::class.java)
            is ExplicitPacket -> packet.id
            else -> registry!!.getPacketId(packet::class.java)
        }
        if (packetId != -1) msg.writeVarInt(packetId) else return
        val packetClazz =
            try {
                if (packet is PacketSnapshot) registry!!.getPacket(registry!!.getPacketId(packet.wrappedPacket.javaClass)) else packet
            } catch (npe: NullPointerException) { null } ?: return
        try {
            if (LimboListener.handleSend(packetClazz, handler)) return
            packet.encode(msg, version)
            MoeLimbo.debug(handler, "Encoding ${"0x%02X".format(packetId)} packet with ${msg.readableBytes()} bytes length")
            MoeLimbo.debug(handler, packetClazz.toString())
        } catch (ex: Exception) { ex.printStackTrace() }
    }

    override fun flush(ctx: ChannelHandlerContext) {
        MoeLimbo.debug(handler, "Output flushed.")
        super.flush(ctx)
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) { ExceptionCatcher.handle(ctx.channel(), cause) }
}