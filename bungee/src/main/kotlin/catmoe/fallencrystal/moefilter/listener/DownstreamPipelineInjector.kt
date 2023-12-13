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

package catmoe.fallencrystal.moefilter.listener

import catmoe.fallencrystal.moefilter.event.packet.BungeeKnownPacketEvent
import catmoe.fallencrystal.moefilter.event.packet.BungeePacketEvent
import catmoe.fallencrystal.moefilter.event.packet.PacketAction
import catmoe.fallencrystal.moefilter.event.packet.PacketDirection
import catmoe.fallencrystal.moefilter.network.bungee.util.ChannelRecord
import catmoe.fallencrystal.moefilter.network.common.ByteMessage
import catmoe.fallencrystal.translation.event.EventManager
import catmoe.fallencrystal.translation.utils.config.LocalConfig
import catmoe.fallencrystal.translation.utils.config.Reloadable
import catmoe.fallencrystal.translation.utils.version.Version
import io.netty.buffer.ByteBuf
import io.netty.channel.Channel
import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import net.md_5.bungee.ServerConnection
import net.md_5.bungee.api.event.ServerConnectedEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.connection.InitialHandler
import net.md_5.bungee.event.EventHandler
import net.md_5.bungee.netty.ChannelWrapper
import net.md_5.bungee.protocol.DefinedPacket
import net.md_5.bungee.protocol.PacketWrapper
import java.util.concurrent.CompletableFuture

object DownstreamPipelineInjector : Listener, Reloadable {

    private const val CH = "ch"
    private const val NAME = "moefilter-downstream-listener"
    private val a = ServerConnection::class.java.getDeclaredField(CH)
    private val b = ChannelWrapper::class.java.getDeclaredField(CH)
    private var inject = false
    private var callKnown = true
    private var callByteBuf = true
    private var callWrite = true
    private var callRead = true

    init {
        a.isAccessible=true
        b.isAccessible=true
    }

    override fun reload() {
        val config = LocalConfig.getConfig().getConfig("packet-listener.downstream")
        inject=config.getBoolean("inject")
        callKnown=config.getBoolean("call-known")
        callByteBuf=config.getBoolean("call-bytebuffer")
        callWrite=config.getBoolean("write")
        callRead=config.getBoolean("read")
    }

    @EventHandler
    fun onServerConnected(event: ServerConnectedEvent) {
        if (!inject) return
        CompletableFuture.runAsync {
            try {
                val initialHandler = event.player.pendingConnection as InitialHandler
                val channel = b[a[event.server]] as Channel
                val pipeline = channel.pipeline()
                pipeline.addBefore("inbound-boss", NAME, DownstreamListener(channel, initialHandler))
                ChannelRecord.putDownstream(event.server, initialHandler, channel)
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }
    }

    internal class DownstreamListener(
        val channel: Channel,
        val handler: InitialHandler,
    ) : ChannelDuplexHandler() {

        val version = Version.of(handler.version)

        override fun write(ctx: ChannelHandlerContext?, msg: Any?, promise: ChannelPromise?) {
            if (callWrite) {
                when (msg) {
                    is ByteBuf -> {
                        if (callByteBuf) {
                            val byteBuf = ByteMessage(msg.copy())
                            val event = BungeePacketEvent(
                                byteBuf.readVarInt(),
                                byteBuf,
                                channel,
                                version,
                                handler,
                                PacketDirection.DOWNSTREAM,
                                PacketAction.WRITE
                            )
                            EventManager.callEvent(event)
                            byteBuf.release()
                            if (event.isCancelled()) return
                        }
                    }

                    is DefinedPacket -> {
                        if (callKnown) {
                            val event = BungeeKnownPacketEvent(
                                msg, channel, version, handler, PacketDirection.DOWNSTREAM, PacketAction.WRITE
                            )
                            EventManager.callEvent(event)
                            if (event.isCancelled()) return
                        }
                    }
                }
            }
            super.write(ctx, msg, promise)
        }

        override fun channelRead(ctx: ChannelHandlerContext?, msg: Any?) {
            if (callRead) {
                run {
                    when (msg) {
                        is ByteBuf -> {
                            if (callByteBuf) {
                                val byteBuf = ByteMessage(msg.copy())
                                val event = BungeePacketEvent(
                                    byteBuf.readVarInt(),
                                    byteBuf,
                                    channel,
                                    version,
                                    handler,
                                    PacketDirection.DOWNSTREAM,
                                    PacketAction.READ
                                )
                                EventManager.callEvent(event)
                                byteBuf.release()
                                if (event.isCancelled()) return
                            }
                        }
                        is PacketWrapper -> {
                            if (callKnown) {
                                val packet = msg.packet ?: return@run
                                val event = BungeeKnownPacketEvent(
                                    packet, channel, version, handler, PacketDirection.DOWNSTREAM, PacketAction.READ
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
    }

}