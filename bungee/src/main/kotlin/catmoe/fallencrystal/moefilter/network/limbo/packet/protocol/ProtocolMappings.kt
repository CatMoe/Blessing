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

package catmoe.fallencrystal.moefilter.network.limbo.packet.protocol

import catmoe.fallencrystal.moefilter.network.limbo.protocol.Mapping
import catmoe.fallencrystal.translation.utils.version.Version
import java.util.*
import java.util.function.Supplier

class ProtocolMappings {
    val registry: MutableMap<Version, PacketRegistry> = EnumMap(Version::class.java)

    fun register(packet: Supplier<*>, vararg mappings: Mapping) {
        for (mapping in mappings) {
            for (ver in getRange(mapping)) {
                val reg = registry.computeIfAbsent(ver!!) { version: Version -> PacketRegistry(version) }
                reg.register(mapping.packetId, packet)
            }
        }
    }

    private fun getRange(mapping: Mapping): Collection<Version?> {
        val from = mapping.from
        var curr: Version? = mapping.to
        if (curr === from) return listOf(from)
        val versions: MutableList<Version?> = LinkedList()
        while (curr !== from) {
            versions.add(curr)
            if (curr != null) { curr = curr.prev }
        }
        versions.add(from)
        return versions
    }
}