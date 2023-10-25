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

package catmoe.fallencrystal.moefilter.util.plugin.luckperms

import catmoe.fallencrystal.moefilter.api.user.displaycache.ReCacheDisplayOnJoin
import catmoe.fallencrystal.translation.event.EventManager
import java.util.*
import kotlin.concurrent.schedule

class LuckPermsRegister {
    private fun isAvailable(): Boolean { return try { net.luckperms.api.LuckPermsProvider.get(); true }
    catch (e: Exception) { false } }

    fun register() { if (isAvailable()) { LuckPermsListener.registerEvent(); EventManager.register(ReCacheDisplayOnJoin()) } else { Timer().schedule(1000) { register() } } }
}