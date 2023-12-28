/*
 * Copyright (C) 2023-2023. CatMoe / Blessing Contributors
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

package net.miaomoe.blessing.nbt.dimension

enum class NbtVersion {
    LEGACY, // < 1.16.1
    V1_16_2, // 1.16.2+
    V1_18_2, // 1.18.2+
    V1_19, // 1.19+
    V1_19_1, // 1.19.1+
    V1_19_4, // 1.19.4+
    V1_20_2; // 1.20.2+

    fun moreOrEqual(version: NbtVersion) = this.ordinal >= version.ordinal
}