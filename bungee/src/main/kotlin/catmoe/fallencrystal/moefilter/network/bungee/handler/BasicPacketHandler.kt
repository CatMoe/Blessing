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
import catmoe.fallencrystal.moefilter.network.bungee.util.ChannelRecord
import catmoe.fallencrystal.moefilter.network.common.ByteMessage
import catmoe.fallencrystal.translation.event.EventManager
import catmoe.fallencrystal.translation.utils.config.LocalConfig
import catmoe.fallencrystal.translation.utils.version.Version
import io.netty.buffer.ByteBuf
import io.netty.channel.Channel
import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.connection.InitialHandler
import net.md_5.bungee.protocol.DefinedPacket
import net.md_5.bungee.protocol.PacketWrapper


class BasicPacketHandler(
    val username: String,
    private val initialHandler: InitialHandler,
    val ctx: ChannelHandlerContext
) : ChannelDuplexHandler() {

    private val version = Version.of(initialHandler.version)
    private val config = LocalConfig.getConfig().getConfig("packet-listener.upstream")
    private val callKnown = config.getBoolean("call-known")
    private val callByteBuf = config.getBoolean("call-bytebuffer")
    private val callWrite = config.getBoolean("write")
    private val callRead = config.getBoolean("read")

    val channel: Channel = ctx.channel()

    init {
        ChannelRecord.putUpstream(initialHandler, ctx.channel())
    }

    override fun write(ctx: ChannelHandlerContext, msg: Any?, promise: ChannelPromise?) {
        if (callWrite) {
            when (msg) {
                is DefinedPacket -> {
                    if (callKnown) {
                        val event = BungeeKnownPacketEvent(
                            msg, channel, version, initialHandler, PacketDirection.UPSTREAM, PacketAction.WRITE
                        )
                        EventManager.callEvent(event)
                        if (event.isCancelled()) return
                    }
                }
                is ByteBuf -> {
                    if (callByteBuf) {
                        val byteBuf = ByteMessage(msg.copy())
                        val event = BungeePacketEvent(
                            byteBuf.readVarInt(),
                            byteBuf,
                            channel,
                            version,
                            initialHandler,
                            PacketDirection.UPSTREAM,
                            PacketAction.WRITE
                        )
                        EventManager.callEvent(event)
                        if (event.isCancelled()) return
                        byteBuf.release()
                    }
                }
            }
        }
        super.write(ctx, msg, promise)
    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any?) {
        if (callRead) {
            run {
                when (msg) {
                    is ByteBuf -> {
                        if (callByteBuf) {
                            val copy = msg.copy()
                            val byteMessage = ByteMessage(copy)
                            val event = BungeePacketEvent(
                                byteMessage.readVarInt(),
                                byteMessage,
                                channel, version,
                                initialHandler,
                                PacketDirection.UPSTREAM,
                                PacketAction.READ
                            )
                            EventManager.callEvent(event)
                            byteMessage.release()
                            if (event.isCancelled()) return
                        }
                    }
                    is PacketWrapper -> {
                        if (callKnown) {
                            val packet = msg.packet ?: return@run
                            val event = BungeeKnownPacketEvent(
                                packet, channel, version, initialHandler,
                                PacketDirection.UPSTREAM, PacketAction.READ
                            )
                            EventManager.callEvent(event)
                            if (event.isCancelled()) return
                        }
                    }
                }
            }
        }
        super.channelRead(ctx, msg)
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        ChannelRecord.invalidate(initialHandler)
        super.channelInactive(ctx)
    }

    companion object {
        private val proxy = ProxyServer.getInstance()
    }

}