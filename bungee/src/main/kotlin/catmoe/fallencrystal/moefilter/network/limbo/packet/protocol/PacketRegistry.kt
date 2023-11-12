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

import catmoe.fallencrystal.moefilter.network.limbo.packet.LimboPacket
import catmoe.fallencrystal.translation.utils.version.Version
import java.util.function.Supplier
import kotlin.reflect.KClass

class PacketRegistry(val version: Version) {
    private val packetsById: MutableMap<Int, Supplier<out LimboPacket>> = HashMap()
    private val packetIdByClass: MutableMap<KClass<out LimboPacket>, Int> = HashMap()

    fun getPacket(packetId: Int): LimboPacket? {
        return packetsById[packetId]?.get()
    }

    fun getPacketId(packetClass: KClass<out LimboPacket>?): Int { return packetIdByClass.getOrDefault(packetClass!!, -1) }

    fun register(packetId: Int, supplier: Supplier<out LimboPacket>) {
        packetsById[packetId] = supplier
        packetIdByClass[supplier.get()::class] = packetId
    }
}