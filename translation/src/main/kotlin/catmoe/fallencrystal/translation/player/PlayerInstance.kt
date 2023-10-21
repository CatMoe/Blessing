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

package catmoe.fallencrystal.translation.player

import catmoe.fallencrystal.translation.TranslationLoader
import catmoe.fallencrystal.translation.event.EventListener
import catmoe.fallencrystal.translation.platform.ProxyPlatform.BUNGEE
import catmoe.fallencrystal.translation.platform.ProxyPlatform.VELOCITY
import catmoe.fallencrystal.translation.player.bungee.BungeePlayerGetter
import catmoe.fallencrystal.translation.player.velocity.VelocityPlayerGetter
import com.github.benmanes.caffeine.cache.Caffeine
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

object PlayerInstance : PlayerGetter, EventListener {

    private val list: MutableCollection<TranslatePlayer> = CopyOnWriteArrayList()
    private val platform = TranslationLoader.instance.loader.platform

    val cacheUUID = Caffeine.newBuilder().build<UUID, TranslatePlayer>()
    val cacheName = Caffeine.newBuilder().build<String, TranslatePlayer>()

    override fun getPlayers(): MutableCollection<TranslatePlayer> { return list }

    override fun getPlayer(uuid: UUID): TranslatePlayer? {
        val a = cacheUUID.getIfPresent(uuid)
        if (a != null) return a
        list.forEach { if (!it.isOnline()) list.remove(it) else if (it.getUniqueId() == uuid) return it }
        val r = when (platform) {
            BUNGEE -> TranslationLoader.secureAccess(BungeePlayerGetter())?.getPlayer(uuid)
            VELOCITY -> TranslationLoader.secureAccess(VelocityPlayerGetter())?.getPlayer(uuid)
        }
        if (r != null) addToList(r)
        return r
    }

    override fun getPlayer(name: String): TranslatePlayer? {
        list.forEach { if (!it.isOnline()) list.remove(it) else if (it.getName() == name) return it }
        val r = when (platform) {
            BUNGEE -> TranslationLoader.secureAccess(BungeePlayerGetter())?.getPlayer(name)
            VELOCITY -> TranslationLoader.secureAccess(VelocityPlayerGetter())?.getPlayer(name)
        }
        if (r != null) addToList(r)
        return r
    }

    fun getCachedOrNull(name: String): TranslatePlayer? { return cacheName.getIfPresent(name.lowercase()) }

    fun getCachedOrNull(uuid: UUID): TranslatePlayer? { return cacheUUID.getIfPresent(uuid) }

    fun addToList(player: TranslatePlayer) {
        list.add(player)
        cacheUUID.put(player.getUniqueId(), player)
        cacheName.put(player.getName().lowercase(), player)
    }

    fun removeFromList(player: TranslatePlayer) {
        var p = if (list.contains(player)) player else null
        list.forEach { if (it.getUniqueId() == player.getUniqueId()) { p=it; return@forEach } }
        val pl = p ?: return
        list.remove(p)
        cacheName.invalidate(pl.getName())
        cacheUUID.invalidate(pl.getUniqueId())
    }

}