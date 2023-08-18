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

package catmoe.fallencrystal.moefilter

import catmoe.fallencrystal.translation.event.EventManager
import catmoe.fallencrystal.translation.event.events.proxy.PlayerJoinEvent
import catmoe.fallencrystal.translation.event.events.proxy.PlayerLeaveEvent
import catmoe.fallencrystal.translation.event.events.proxy.PlayerPostBrandEvent
import catmoe.fallencrystal.translation.player.PlayerInstance
import catmoe.fallencrystal.translation.player.TranslatePlayer
import catmoe.fallencrystal.translation.player.velocity.VelocityPlayer
import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.connection.PostLoginEvent
import com.velocitypowered.api.event.player.PlayerChatEvent
import com.velocitypowered.api.event.player.PlayerClientBrandEvent

class ConversionListener(val plugin: MoeFilterVelocity) {

    @Subscribe
    fun onPostLogin(event: PostLoginEvent) {
        val player = TranslatePlayer(VelocityPlayer(event.player))
        PlayerInstance.cacheUUID.put(player.getUniqueId(), player)
        PlayerInstance.cacheName.put(player.getName(), player)
        EventManager.callEvent(PlayerJoinEvent(player))
    }

    @Subscribe
    fun onLeave(event: DisconnectEvent) {
        val player = PlayerInstance.getOrNull(event.player.uniqueId) ?: return
        EventManager.callEvent(PlayerLeaveEvent(player))
        PlayerInstance.cacheUUID.invalidate(player.getUniqueId())
        PlayerInstance.cacheName.invalidate(player.getName())
    }

    @Subscribe
    fun postBrand(event: PlayerClientBrandEvent) {
        val player = PlayerInstance.getOrNull(event.player.uniqueId) ?: return
        EventManager.callEvent(PlayerPostBrandEvent(player, event.brand))
    }

    @Subscribe(order = PostOrder.LAST)
    fun chatEvent(event: PlayerChatEvent) {
        val player = PlayerInstance.getOrNull(event.player.uniqueId) ?: return
        val e = catmoe.fallencrystal.translation.event.events.proxy.PlayerChatEvent(player, event.message)
        if (!event.result.isAllowed) e.setCancelled()
        EventManager.callEvent(e)
        if (e.isCancelled()) { event.result=PlayerChatEvent.ChatResult.denied() }
        else event.result=PlayerChatEvent.ChatResult.message(e.message)
    }

}