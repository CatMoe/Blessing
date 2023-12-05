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

package catmoe.fallencrystal.moefilter.network.limbo.check.impl

import catmoe.fallencrystal.moefilter.network.common.ServerType
import catmoe.fallencrystal.moefilter.network.common.kick.DisconnectType
import catmoe.fallencrystal.moefilter.network.common.kick.FastDisconnect
import catmoe.fallencrystal.moefilter.network.limbo.check.AntiBotChecker
import catmoe.fallencrystal.moefilter.network.limbo.check.LimboCheckType
import catmoe.fallencrystal.moefilter.network.limbo.check.LimboChecker
import catmoe.fallencrystal.moefilter.network.limbo.handler.LimboHandler
import catmoe.fallencrystal.moefilter.network.limbo.listener.ListenPacket
import catmoe.fallencrystal.moefilter.network.limbo.packet.LimboPacket
import catmoe.fallencrystal.moefilter.network.limbo.packet.c2s.PacketClientChat
import catmoe.fallencrystal.translation.event.EventListener
import catmoe.fallencrystal.translation.event.EventManager
import catmoe.fallencrystal.translation.event.annotations.AsynchronousHandler
import catmoe.fallencrystal.translation.event.annotations.EventHandler
import catmoe.fallencrystal.translation.event.annotations.HandlerPriority
import catmoe.fallencrystal.translation.event.annotations.IgnoreCancelled
import catmoe.fallencrystal.translation.event.events.player.PlayerChatEvent
import catmoe.fallencrystal.translation.event.events.player.PlayerLeaveEvent
import catmoe.fallencrystal.translation.event.events.player.PlayerPostBrandEvent
import catmoe.fallencrystal.translation.player.TranslatePlayer
import catmoe.fallencrystal.translation.player.bungee.BungeePlayer
import catmoe.fallencrystal.translation.player.velocity.VelocityPlayer
import catmoe.fallencrystal.translation.utils.config.LocalConfig
import com.github.benmanes.caffeine.cache.Caffeine

@AntiBotChecker(LimboCheckType.CHAT)
@ListenPacket(PacketClientChat::class)
object ChatCheck : LimboChecker, EventListener {

    private val connected = Caffeine.newBuilder().build<TranslatePlayer, Boolean>()
    private var enable = LocalConfig.getLimbo().getBoolean("disable-chat")

    override fun reload() {
        enable = LocalConfig.getLimbo().getBoolean("disable-chat")
    }

    /*
    使用Brand来检测玩家是否已经连接到服务器 大多数SpamBot不会发送PluginMessage
    @EventHandler(PlayerConnectServerEvent::class, priority = HandlerPriority.HIGHEST)
    fun serverConnect(event: PlayerConnectServerEvent) { if (event.isConnected) connected.put(event.player, true) }
     */

    @EventHandler(PlayerPostBrandEvent::class, priority = HandlerPriority.LOWEST)
    @AsynchronousHandler
    fun postBrand(event: PlayerPostBrandEvent) { connected.put(event.player, true) }

    @EventHandler(PlayerLeaveEvent::class, HandlerPriority.LOWEST)
    fun serverDisconnected(event: PlayerLeaveEvent) { connected.invalidate(event.player) }

    @EventHandler(PlayerChatEvent::class)
    @IgnoreCancelled
    fun chat(event: PlayerChatEvent) {
        if (connected.getIfPresent(event.player) != true && enable) {
            when (val upstream = event.player.upstream) {
                is BungeePlayer -> upstream.channel()?.let { FastDisconnect.disconnect(it, DisconnectType.CANNOT_CHAT, ServerType.BUNGEE_CORD) }
                is VelocityPlayer -> {
                    val reason = FastDisconnect.reasonCache.getIfPresent(DisconnectType.CANNOT_CHAT)?.component
                    if (reason == null) upstream.disconnect() else upstream.disconnect(reason)
                }
            }
        }
    }

    override fun received(packet: LimboPacket, handler: LimboHandler, cancelledRead: Boolean): Boolean {
        if (!enable) return false
        FastDisconnect.disconnect(handler, DisconnectType.CANNOT_CHAT)
        return true
    }

    override fun send(packet: LimboPacket, handler: LimboHandler, cancelled: Boolean) = false

    override fun register() { EventManager.register(this) }

    override fun unregister() { EventManager.unregister(this) }
}