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

package net.miaomoe.blessing.fallback.cache

import net.miaomoe.blessing.fallback.handler.FallbackHandler
import net.miaomoe.blessing.protocol.packet.play.PacketChunk
import net.miaomoe.blessing.protocol.util.Position
import net.miaomoe.blessing.protocol.version.Version
import net.miaomoe.blessing.protocol.version.VersionRange

@Suppress("MemberVisibilityCanBePrivate")
class ChunksCache(position: List<Position>) {

    val caches = position.map { position ->
        val chunk = PacketChunk(position.x.toInt(), position.z.toInt())
        PacketCacheGroup(
            chunk,
            "Cached chunk packet (x=${chunk.x}, z=${chunk.z})",
            true, VersionRange.of(Version.V1_7_6, Version.V1_20_3))
    }.toMutableList()

    fun write(handler: FallbackHandler) {
        for (cache in caches) handler.write(cache, false)
    }

    companion object {

        fun surround(position: Position, range: Int) : ChunksCache {
            val positions = mutableListOf<Position>()
            for (x in position.x.toInt().let { it - range .. it + range })
                for (z in position.z.toInt().let { it - range .. it + range }) {
                    positions.add(Position(x.toDouble(), 0.0, z.toDouble()))
                }
            return ChunksCache(positions)
        }

    }

}