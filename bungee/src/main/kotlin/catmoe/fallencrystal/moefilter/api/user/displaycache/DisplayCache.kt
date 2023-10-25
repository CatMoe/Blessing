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

package catmoe.fallencrystal.moefilter.api.user.displaycache

import catmoe.fallencrystal.moefilter.MoeFilterBungee
import com.github.benmanes.caffeine.cache.Caffeine
import net.luckperms.api.LuckPermsProvider
import net.md_5.bungee.api.ProxyServer
import java.util.*

object DisplayCache {
    private val displayCache = Caffeine.newBuilder().build<UUID, Display>()
    fun getDisplay(uuid: UUID): Display {
        if (displayCache.getIfPresent(uuid) == null) { try { updateFromUUID(uuid) } catch (npe: NullPointerException) { return Display(uuid, "", "") } }
        return displayCache.getIfPresent(uuid) ?: Display(uuid, "", "")
    }
    fun updateDisplayCache(uuid: UUID, display: Display) { if (displayCache.getIfPresent(uuid) != null) { displayCache.invalidate(uuid) }; displayCache.put(uuid, display) }

    private fun updateFromUUID(uuid: UUID) {
        ProxyServer.getInstance().scheduler.runAsync(MoeFilterBungee.instance) {
            val metaData = LuckPermsProvider.get().userManager.getUser(uuid)!!.cachedData.metaData
            val prefix = metaData.prefix
            val suffix = metaData.suffix
            updateDisplayCache(uuid, Display(uuid, prefix ?: "", suffix ?: ""))
        }
    }
}