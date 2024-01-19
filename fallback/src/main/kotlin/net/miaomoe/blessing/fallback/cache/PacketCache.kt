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

import net.miaomoe.blessing.fallback.packet.ByteArrayHolder
import net.miaomoe.blessing.protocol.packet.type.PacketToClient
import net.miaomoe.blessing.protocol.util.ByteMessage
import net.miaomoe.blessing.protocol.version.Version
import kotlin.reflect.KClass

class PacketCache(
    val kClass: KClass<out PacketToClient>,
    override val byteArray: ByteArray? = null,
    val description: String? = null
) : PacketToClient, ByteArrayHolder {
    companion object {
        @JvmStatic
        @JvmOverloads
        fun create(packet: PacketToClient, version: Version, description: String? = null) =
            PacketCache(packet::class, ByteMessage.create().use { packet.encode(it, version); it.toByteArray() }, description)
    }
}