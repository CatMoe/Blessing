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

package catmoe.fallencrystal.translation.event.events.player

import catmoe.fallencrystal.translation.event.TranslationEvent
import catmoe.fallencrystal.translation.player.TranslatePlayer
import catmoe.fallencrystal.translation.server.TranslateServer

@Suppress("MemberVisibilityCanBePrivate")
class PlayerConnectServerEvent(
    val isConnected: Boolean,
    val server: TranslateServer,
    val player: TranslatePlayer
) : TranslationEvent() {

    var isCancelled = false

    override fun isCancelled(): Boolean { return isCancelled }

    override fun setCancelled() {
        if (isConnected) throw IllegalAccessException("Cannot set canceled when player is connected.")
        isCancelled=true
    }

}