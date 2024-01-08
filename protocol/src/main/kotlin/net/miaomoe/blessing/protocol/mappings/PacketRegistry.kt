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
import net.miaomoe.blessing.protocol.packet.type.MinecraftPacket
import net.miaomoe.blessing.protocol.version.Version
import kotlin.reflect.KClass

class PacketRegistry(val version: Version) {
    private val idMappings = Caffeine
        .newBuilder()
        .build<Int, PacketMapping>()
    private val classMappings = Caffeine
        .newBuilder()
        .build<KClass<out MinecraftPacket>, PacketMapping>()

    private fun getException(obj: Any) = NullPointerException("Cannot found packet with $obj! (${version.name})")

    @Throws(NullPointerException::class)
    fun getPacket(id: Int) = idMappings.getIfPresent(id)?.init?.get() ?: getException(id)
    @Throws(NullPointerException::class)
    fun getPacket(clazz: KClass<out MinecraftPacket>) = classMappings.getIfPresent(clazz)?.init?.get() ?: getException(clazz.qualifiedName!!)

    fun register(id: Int, mapping: PacketMapping) {
        this.idMappings.put(id, mapping)
        this.classMappings.put(
            mapping.init.get()::class,
            mapping
        )
    }
}