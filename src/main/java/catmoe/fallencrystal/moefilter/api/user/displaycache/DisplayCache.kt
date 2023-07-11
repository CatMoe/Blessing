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

package catmoe.fallencrystal.moefilter.api.user.displaycache

import catmoe.fallencrystal.moefilter.MoeFilter
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
        ProxyServer.getInstance().scheduler.runAsync(MoeFilter.instance) {
            val metaData = LuckPermsProvider.get().userManager.getUser(uuid)!!.cachedData.metaData
            val prefix = metaData.prefix
            val suffix = metaData.suffix
            updateDisplayCache(uuid, Display(uuid, prefix ?: "", suffix ?: ""))
        }
    }
}