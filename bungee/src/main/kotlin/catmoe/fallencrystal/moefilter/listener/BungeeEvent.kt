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

import catmoe.fallencrystal.moefilter.common.state.StateManager
import catmoe.fallencrystal.moefilter.network.bungee.util.PipelineUtil
import catmoe.fallencrystal.moefilter.util.message.notification.Notifications
import catmoe.fallencrystal.translation.event.EventManager
import catmoe.fallencrystal.translation.event.events.player.*
import catmoe.fallencrystal.translation.player.PlayerInstance
import catmoe.fallencrystal.translation.player.TranslatePlayer
import catmoe.fallencrystal.translation.player.bungee.BungeePlayer
import catmoe.fallencrystal.translation.server.ServerInstance
import catmoe.fallencrystal.translation.server.TranslateServer
import catmoe.fallencrystal.translation.server.bungee.BungeeServer
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.config.ServerInfo
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.event.*
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import net.md_5.bungee.event.EventPriority

class BungeeEvent : Listener {

    private val proxy = ProxyServer.getInstance()

    @EventHandler(priority = EventPriority.LOWEST)
    fun onChat(event: ChatEvent) {
        val player = ProxyServer.getInstance().getPlayer(event.sender.toString())
        val p = PlayerInstance.getCachedOrNull(player.uniqueId) ?: return
        val e = PlayerChatEvent(p, event.message, event.isProxyCommand)
        if (event.isCancelled) e.setCancelled()
        EventManager.callEvent(e)
        if (!event.isCancelled && e.isCancelled()) event.isCancelled=true
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onPostLogin(event: PostLoginEvent) {
        val player = TranslatePlayer(BungeePlayer(event.player))
        PlayerInstance.addToList(player)
        EventManager.callEvent(PlayerJoinEvent(player))
        if (StateManager.inAttack.get() && player.hasPermission("moefilter.notifications.auto"))
            Notifications.autoNotification.add(event.player)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onServerConnect(event: ServerConnectEvent) {
        val e = PlayerConnectServerEvent(false, getTranslateServer(event.target), getTranslatePlayer(event.player))
        e.isCancelled=event.isCancelled
        EventManager.callEvent(e)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onServerConnect(event: ServerConnectedEvent) {
        EventManager.callEvent(PlayerConnectServerEvent(true, getTranslateServer(event.server.info), getTranslatePlayer(event.player)))
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onServerSwitch(event: ServerSwitchEvent) {
        val from = event.from ?: return
        EventManager.callEvent(PlayerSwitchServerEvent(getTranslatePlayer(event.player), getTranslateServer(from), getTranslateServer(event.player.server.info)))
    }

    private fun getTranslateServer(s: ServerInfo) = ServerInstance.getServer(s.name) ?: TranslateServer(BungeeServer(s))

    private fun getTranslatePlayer(p: ProxiedPlayer) = PlayerInstance.getPlayer(p.uniqueId) ?: TranslatePlayer(BungeePlayer(p))

    @EventHandler(priority = EventPriority.LOWEST)
    fun onDisconnect(event: PlayerDisconnectEvent) {
        PipelineUtil.invalidateChannel(event.player)
        val player = PlayerInstance.getCachedOrNull(event.player.name) ?: return
        EventManager.callEvent(PlayerLeaveEvent(player))
        PlayerInstance.removeFromList(player)
    }
}