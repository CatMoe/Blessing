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

package catmoe.fallencrystal.translation.event.events.proxy

import catmoe.fallencrystal.translation.event.TranslationEvent
import catmoe.fallencrystal.translation.player.TranslatePlayer
import java.util.concurrent.atomic.AtomicBoolean

class PlayerChatEvent(
    val player: TranslatePlayer,
    val message: String,
    val isProxyCommand: Boolean /* Testing, see https://github.com/CatMoe/MoeFilter/issues/48 */
) : TranslationEvent() {
    private var c = AtomicBoolean(false)

    fun isCommand(): Boolean { return message.isNotEmpty() && message.startsWith("/") }

    override fun setCancelled() { c.set(true) }

    override fun isCancelled(): Boolean { return c.get() }
}