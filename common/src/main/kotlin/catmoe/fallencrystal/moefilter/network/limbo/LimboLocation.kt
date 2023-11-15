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

package catmoe.fallencrystal.moefilter.network.limbo

class LimboLocation(
    val x: Double,
    val y: Double,
    val z: Double,
    val yaw: Float,
    val pitch: Float,
    val onGround: Boolean
) {
    override fun toString() = "LimboLocation(x=$x, y=$y, z=$z, yaw=$yaw, pitch=$pitch, onGround=$onGround)"

    fun equal(location: LimboLocation, compareAxisOnly: Boolean = false): Boolean {
        return when (compareAxisOnly) {
            true -> this.x == location.x && this.y == location.y && this.z == location.z
            false -> this.equal(location, true) && this.yaw == location.yaw  && this.pitch == location.pitch && this.onGround == location.onGround
        }
    }

}