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

import com.github.benmanes.caffeine.cache.Caffeine
import net.miaomoe.blessing.protocol.packet.type.PacketInterface
import net.miaomoe.blessing.protocol.packet.type.PacketToClient
import net.miaomoe.blessing.protocol.version.Version
import net.miaomoe.blessing.protocol.version.VersionRange
import java.util.function.Supplier
import kotlin.reflect.KClass

@Suppress("MemberVisibilityCanBePrivate")
class ProtocolMappings() {

    private val registry = Caffeine
        .newBuilder()
        .build<Version, PacketRegistry>()

    fun register(mapping: PacketMapping) {
        for ((range, packetId) in mapping.list) {
            for (version in range) {
                val registry = this.registry.getIfPresent(version) ?: PacketRegistry(version)
                registry.register(packetId, mapping)
                this.registry.put(version, registry)
            }
        }
    }

    fun register(init: Supplier<out PacketInterface>, list: Map<VersionRange, Int>)
    = this.register(PacketMapping.generate(init, list))

    @Throws(NullPointerException::class)
    private fun getRegistryFromVersion(version: Version) = this.registry.getIfPresent(version)
        ?: throw NullPointerException("Mappings for this version (${version.name}) is null!")

    @Throws(NullPointerException::class)
    fun getMappings(version: Version, id: Int) = id.let(getRegistryFromVersion(version)::getMapping)
    @Throws(NullPointerException::class)
    fun getMappings(version: Version, `class`: KClass<out PacketToClient>): PacketMapping {
        val registry = getRegistryFromVersion(version)
        return registry.getMapping(registry.getId(`class`))
    }

    fun getId(version: Version, `class`: KClass<out PacketToClient>): Int = `class`.let(getRegistryFromVersion(version)::getId)

}