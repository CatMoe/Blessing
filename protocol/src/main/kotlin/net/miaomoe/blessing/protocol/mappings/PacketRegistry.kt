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

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import net.miaomoe.blessing.protocol.packet.type.PacketInterface
import net.miaomoe.blessing.protocol.util.LazyInit
import net.miaomoe.blessing.protocol.version.Version
import kotlin.reflect.KClass

@Suppress("MemberVisibilityCanBePrivate")
class PacketRegistry(val version: Version) {
    private val idMappings = create<Int, PacketMapping>()
    private val classMappings = create<KClass<out PacketInterface>, Int>()

    fun getMapping(id: Int): PacketMapping {
        require(idMappings.isAlreadyLoaded) { "idMappings is not loaded!" }
        return idMappings.value.getIfPresent(id) ?: throw NullPointerException("Cannot found mappings for id $id")
    }

    fun getId(`class`: KClass<out PacketInterface>): Int {
        require(classMappings.isAlreadyLoaded) { "classMappings is not loaded!" }
        return classMappings.value.getIfPresent(`class`) ?: throw NullPointerException("Cannot  found id for class ${`class`.qualifiedName}")
    }

    fun register(packetId: Int, mapping: PacketMapping) {
        this.idMappings.value.put(packetId, mapping)
        this.classMappings.value.put(mapping.init.get()::class, packetId)
    }

    companion object {
        fun <K, V>create(): LazyInit<Cache<K, V>> = LazyInit { Caffeine.newBuilder().build() }
    }
}