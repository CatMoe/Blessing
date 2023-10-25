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