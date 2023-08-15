/*
 * Copyright 2023. CatMoe / FallenCrystal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package catmoe.fallencrystal.moefilter.network.limbo.packet.protocol

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