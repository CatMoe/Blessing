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

package net.miaomoe.blessing.protocol.mappings

import net.miaomoe.blessing.protocol.packet.type.PacketInterface
import net.miaomoe.blessing.protocol.version.Version
import net.miaomoe.blessing.protocol.version.VersionRange
import java.util.function.Supplier

data class PacketMapping(val init: Supplier<out PacketInterface>, val list: Map<VersionRange, Int>) {
    companion object {

        @JvmStatic
        fun generate(init: Supplier<out PacketInterface>, list: Map<VersionRange, Int>) =
            PacketMapping(init, list)

        @JvmStatic
        fun builder() = BuilderUtil()

        @JvmStatic
        fun withAll(id: Int) = withSingle(VersionRange.allVersion, id)

        @JvmStatic
        fun withSingle(range: VersionRange, id: Int) = mapOf(range to id)

        @JvmStatic
        fun withSingle(from: Version, to: Version, id: Int) = withSingle(VersionRange.of(from, to), id)
    }

    @Suppress("MemberVisibilityCanBePrivate")
    class BuilderUtil {
        private val map = mutableMapOf<VersionRange, Int>()

        fun addMapping(id: Int, from: Version, to: Version) =
            addMapping(id, VersionRange.of(from, to))

        fun addMapping(id: Int, range: VersionRange): BuilderUtil {
            map[range] = id
            return this
        }

        fun addMapping(id: Int, version: Version) =
            addMapping(id, VersionRange.of(version))

        fun getMapping() = map.toMap()
    }
}