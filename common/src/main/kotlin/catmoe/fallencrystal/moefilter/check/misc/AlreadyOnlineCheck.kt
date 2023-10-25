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

package catmoe.fallencrystal.moefilter.check.misc

import catmoe.fallencrystal.moefilter.check.AbstractCheck
import catmoe.fallencrystal.moefilter.check.info.CheckInfo
import catmoe.fallencrystal.moefilter.check.info.impl.Joining
import catmoe.fallencrystal.translation.player.PlayerInstance

class AlreadyOnlineCheck : AbstractCheck() {
    //override fun increase(info: CheckInfo): Boolean { return BungeeCord.getInstance().getPlayer((info as Joining).username) != null }
    override fun increase(info: CheckInfo): Boolean {
        return PlayerInstance.getPlayer((info as Joining).username) != null
    }
}