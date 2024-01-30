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

package net.miaomoe.blessing.protocol.message

enum class BossBarFlags(val mask: Int) {
    DARKEN_SKY(1),
    DRAGON_BAR(2),
    CREATE_FOG(4);

    companion object {
        fun toFlags(list: List<BossBarFlags>): Int {
            var flags = 0
            list.distinct().forEach { flags = flags or it.mask }
            return flags
        }

        fun fromFlag(id: Int): List<BossBarFlags> {
            val decodedFlags = mutableListOf<BossBarFlags>()
            for (flag in entries) {
                if (id and flag.mask != 0) decodedFlags.add(flag)
            }
            return decodedFlags
        }
    }
}