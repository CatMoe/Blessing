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

package catmoe.fallencrystal.moefilter.network.limbo.packet.handshake

import catmoe.fallencrystal.moefilter.network.limbo.packet.LimboPacket
import catmoe.fallencrystal.moefilter.network.limbo.util.Version
import java.util.function.Supplier

class PacketRegistry(val version: Version) {
    private val packetsById: MutableMap<Int, Supplier<*>> = HashMap()
    private val packetIdByClass: MutableMap<Class<*>, Int> = HashMap()

    fun getPacket(packetId: Int): LimboPacket? {
        val supplier = packetsById[packetId]
        return if (supplier == null) null else supplier.get() as LimboPacket
    }

    fun getPacketId(packetClass: Class<*>?): Int { return packetIdByClass.getOrDefault(packetClass!!, -1) }

    fun register(packetId: Int, supplier: Supplier<*>) {
        packetsById[packetId] = supplier
        packetIdByClass[supplier.get().javaClass] = packetId
    }
}