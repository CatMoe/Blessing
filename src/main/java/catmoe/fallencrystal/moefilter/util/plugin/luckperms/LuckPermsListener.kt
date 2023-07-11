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

package catmoe.fallencrystal.moefilter.util.plugin.luckperms

import catmoe.fallencrystal.moefilter.MoeFilter
import catmoe.fallencrystal.moefilter.api.event.EventManager
import catmoe.fallencrystal.moefilter.api.user.displaycache.Display
import catmoe.fallencrystal.moefilter.api.user.displaycache.DisplayCache
import catmoe.fallencrystal.moefilter.api.user.displaycache.ReCacheDisplayOnJoin
import net.luckperms.api.LuckPermsProvider
import net.luckperms.api.event.user.UserDataRecalculateEvent

object LuckPermsListener {

    fun registerEvent() {
        val luckperms = LuckPermsProvider.get()
        val plugin = MoeFilter.instance

        EventManager.registerListener(plugin, ReCacheDisplayOnJoin())

        // private fun onUserDataRecalculateEvent -> UserDataRecalculateEvent
        luckperms.eventBus.subscribe(plugin, UserDataRecalculateEvent::class.java, LuckPermsListener::onUserDataRecalculateEvent)
    }

    private fun onUserDataRecalculateEvent(event: UserDataRecalculateEvent) {
        DisplayCache.updateDisplayCache(event.user.uniqueId, Display(event.user.uniqueId, event.user.cachedData.metaData.prefix ?: "", event.user.cachedData.metaData.suffix ?: ""))
    }
}