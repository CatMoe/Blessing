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

package net.miaomoe.blessing.protocol.version

@Suppress("MemberVisibilityCanBePrivate")
data class VersionRange(val min: Version, val max: Version) : Iterable<Version> {
    private val list = Version.entries.filter { it.registerMap && it.moreOrEqual(min) && it.lessOrEqual(max) }
    fun inRange(version: Version) = list.contains(version)
    fun toList() = list
    @Deprecated("toIntRange should not be applied to the protocol.", replaceWith = ReplaceWith("toList"))
    fun toIntRange() = min.protocolId..max.protocolId

    override fun iterator(): Iterator<Version> {
        return object : Iterator<Version> {
            private val list = this@VersionRange.list
            private var readerIndex = 0
            override fun hasNext() = (readerIndex < list.size)
            override fun next() = list[readerIndex++ % list.size]
        }
    }
}