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

package catmoe.fallencrystal.moefilter

import catmoe.fallencrystal.translation.event.EventManager
import catmoe.fallencrystal.translation.event.events.player.PlayerJoinEvent
import catmoe.fallencrystal.translation.event.events.player.PlayerLeaveEvent
import catmoe.fallencrystal.translation.event.events.player.PlayerPostBrandEvent
import catmoe.fallencrystal.translation.player.PlayerInstance
import catmoe.fallencrystal.translation.player.TranslatePlayer
import catmoe.fallencrystal.translation.player.velocity.VelocityPlayer
import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.connection.PostLoginEvent
import com.velocitypowered.api.event.player.PlayerChatEvent
import com.velocitypowered.api.event.player.PlayerClientBrandEvent

class ConversionListener(private val plugin: MoeFilterVelocity) {

    @Subscribe
    fun onPostLogin(event: PostLoginEvent) {
        val player = TranslatePlayer(VelocityPlayer(event.player))
        PlayerInstance.cacheUUID.put(player.getUniqueId(), player)
        PlayerInstance.cacheName.put(player.getName(), player)
        EventManager.callEvent(PlayerJoinEvent(player))
    }

    @Subscribe
    fun onLeave(event: DisconnectEvent) {
        val player = PlayerInstance.getCachedOrNull(event.player.uniqueId) ?: return
        EventManager.callEvent(PlayerLeaveEvent(player))
        PlayerInstance.cacheUUID.invalidate(player.getUniqueId())
        PlayerInstance.cacheName.invalidate(player.getName())
    }

    @Subscribe
    fun postBrand(event: PlayerClientBrandEvent) {
        val player = PlayerInstance.getCachedOrNull(event.player.uniqueId) ?: return
        EventManager.callEvent(PlayerPostBrandEvent(player, event.brand))
    }

    @Subscribe(order = PostOrder.LAST)
    fun chatEvent(event: PlayerChatEvent) {
        val command = Regex("/([^/\\s]+)(?=\\s|$)").findAll(event.message)
        var isProxyCommand = false
        val cm = plugin.proxyServer.commandManager
        for (it in command) {
            val c = it.value.replace("/", "")
            val has = cm.hasCommand(c)
            if (has) { isProxyCommand=true; break }
        }
        val player = PlayerInstance.getCachedOrNull(event.player.uniqueId) ?: return
        val e =
            catmoe.fallencrystal.translation.event.events.player.PlayerChatEvent(player, event.message, isProxyCommand)
        if (!event.result.isAllowed) e.setCancelled()
        EventManager.callEvent(e)
        if (e.isCancelled()) { event.result=PlayerChatEvent.ChatResult.denied() }
        else event.result=PlayerChatEvent.ChatResult.message(e.message)
    }

}