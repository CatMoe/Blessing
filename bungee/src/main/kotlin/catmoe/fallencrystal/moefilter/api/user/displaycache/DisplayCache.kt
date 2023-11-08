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

import com.github.benmanes.caffeine.cache.Caffeine
import net.luckperms.api.LuckPermsProvider
import java.util.*

object DisplayCache {
    private val displayCache = Caffeine.newBuilder().build<UUID, Display>()

    fun getDisplay(uuid: UUID) = if (displayCache.getIfPresent(uuid) == null) updateFromUUID(uuid) else displayCache.getIfPresent(uuid)!!
    fun updateDisplayCache(uuid: UUID, display: Display) = displayCache.put(uuid, display)

    private fun updateFromUUID(uuid: UUID): Display {
        val metaData = LuckPermsProvider.get().userManager.getUser(uuid)!!.cachedData.metaData
        val prefix = metaData.prefix
        val suffix = metaData.suffix
        val display = Display(uuid, prefix ?: "", suffix ?: "")
        updateDisplayCache(uuid, display)
        return display
    }
}