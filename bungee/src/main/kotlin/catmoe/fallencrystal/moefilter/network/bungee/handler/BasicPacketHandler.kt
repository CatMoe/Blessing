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

package catmoe.fallencrystal.moefilter.network.bungee.handler

import catmoe.fallencrystal.moefilter.event.packet.BungeeKnownPacketEvent
import catmoe.fallencrystal.moefilter.event.packet.BungeePacketEvent
import catmoe.fallencrystal.moefilter.event.packet.PacketAction
import catmoe.fallencrystal.moefilter.event.packet.PacketDirection
import catmoe.fallencrystal.moefilter.network.common.ByteMessage
import catmoe.fallencrystal.translation.event.EventManager
import catmoe.fallencrystal.translation.utils.version.Version
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.connection.InitialHandler
import net.md_5.bungee.protocol.DefinedPacket
import net.md_5.bungee.protocol.PacketWrapper


class BasicPacketHandler(val username: String, private val initialHandler: InitialHandler) : ChannelDuplexHandler() {

    private val version = Version.of(initialHandler.version)

    override fun write(ctx: ChannelHandlerContext, msg: Any?, promise: ChannelPromise?) {
        when (msg) {
            is DefinedPacket -> {
                val event = BungeeKnownPacketEvent(
                    msg, ctx, version, initialHandler, PacketDirection.UPSTREAM, PacketAction.WRITE
                )
                EventManager.callEvent(event)
                if (event.isCancelled()) return
            }
            is ByteBuf -> {
                val byteBuf = ByteMessage(msg.copy())
                val event = BungeePacketEvent(
                    byteBuf.readVarInt(),
                    byteBuf,
                    ctx,
                    version,
                    initialHandler,
                    PacketDirection.UPSTREAM,
                    PacketAction.WRITE
                )
                EventManager.callEvent(event)
                if (event.isCancelled()) return
            }
        }
        super.write(ctx, msg, promise)
    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any?) {
        run {
            if (msg is ByteBuf) {
                val copy = msg.copy()
                val byteMessage = ByteMessage(copy)
                val event = BungeePacketEvent(
                    byteMessage.readVarInt(),
                    byteMessage,
                    ctx, version,
                    initialHandler,
                    PacketDirection.UPSTREAM,
                    PacketAction.READ
                )
                EventManager.callEvent(event)
                byteMessage.release()
                if (event.isCancelled()) return
            }
            if (msg is PacketWrapper) {
                val packet = msg.packet ?: return@run
                val event = BungeeKnownPacketEvent(
                    packet, ctx, version, initialHandler,
                    PacketDirection.UPSTREAM, PacketAction.READ
                )
                EventManager.callEvent(event)
                if (event.isCancelled()) return
            }
        }
        super.channelRead(ctx, msg)
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        super.channelInactive(ctx)
    }

    companion object {
        private val proxy = ProxyServer.getInstance()
    }

}