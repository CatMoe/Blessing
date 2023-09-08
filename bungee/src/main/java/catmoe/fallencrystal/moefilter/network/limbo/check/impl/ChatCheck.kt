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

package catmoe.fallencrystal.moefilter.network.limbo.check.impl

import catmoe.fallencrystal.moefilter.network.common.ServerType
import catmoe.fallencrystal.moefilter.network.common.kick.DisconnectType
import catmoe.fallencrystal.moefilter.network.common.kick.FastDisconnect
import catmoe.fallencrystal.moefilter.network.limbo.check.Checker
import catmoe.fallencrystal.moefilter.network.limbo.check.LimboCheckType
import catmoe.fallencrystal.moefilter.network.limbo.check.LimboChecker
import catmoe.fallencrystal.moefilter.network.limbo.handler.LimboHandler
import catmoe.fallencrystal.moefilter.network.limbo.listener.HandlePacket
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

@Checker(LimboCheckType.CHAT)
@HandlePacket(PacketClientChat::class)
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
        if (connected.getIfPresent(event.player) != true) {
            when (val upstream = event.player.upstream) {
                is BungeePlayer -> { FastDisconnect.disconnect(upstream.channel(), DisconnectType.CANNOT_CHAT, ServerType.BUNGEE_CORD) }
                is VelocityPlayer -> {
                    val reason = FastDisconnect.reasonCache.getIfPresent(DisconnectType.CANNOT_CHAT)?.component
                    if (reason == null) upstream.disconnect() else upstream.disconnect(reason)
                }
            }
        }
    }

    override fun received(packet: LimboPacket, handler: LimboHandler, cancelledRead: Boolean): Boolean {
        FastDisconnect.disconnect(handler, DisconnectType.CANNOT_CHAT)
        return true
    }

    override fun send(packet: LimboPacket, handler: LimboHandler, cancelled: Boolean): Boolean { return false }

    override fun register() { EventManager.register(this) }

    override fun unregister() { EventManager.unregister(this) }
}