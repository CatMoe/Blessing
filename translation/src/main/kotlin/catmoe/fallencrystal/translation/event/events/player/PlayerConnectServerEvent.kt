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