/*
 * Copyright (C) 2023-2024. CatMoe / Blessing Contributors
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

package net.miaomoe.blessing.protocol.util

import kotlin.math.pow

object PositionUtil {

    fun getLegacyBlockPosition(position: Position) = (position.x.toInt() shl 12 or position.z.toInt() shl 8 or position.y.toInt())

    fun getModernBlockPosition(position: Position) = (position.x.toInt() shl 8 or position.z.toInt() shl 4 or position.y.toInt().let { it - (it shr 4 shl 4) })

    fun getFallGravity(tick: Int) = -((0.98.pow(tick.toDouble()) - 1) * 3.92)

    fun getLegacySpawnPosition(position: Position) = (position.x.toInt() and 0x3FFFFFF shl 38) or (position.y.toInt() and 0xFFF shl 26) or (position.z.toInt() and 0x3FFFFFF)

    fun getModernSpawnPosition(position: Position) = (position.x.toInt() and 0x3FFFFFF shl 38) or (position.y.toInt() and 0x3FFFFFF shl 12) or (position.z.toInt() and 0xFFF)

}