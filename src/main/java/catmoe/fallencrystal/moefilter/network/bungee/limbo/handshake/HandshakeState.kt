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
package catmoe.fallencrystal.moefilter.network.bungee.limbo.handshake

import catmoe.fallencrystal.moefilter.network.bungee.limbo.handshake.Version.Companion.max
import catmoe.fallencrystal.moefilter.network.bungee.limbo.handshake.Version.Companion.min
import catmoe.fallencrystal.moefilter.network.bungee.limbo.packet.c2s.PacketHandshake
import catmoe.fallencrystal.moefilter.network.bungee.limbo.packet.c2s.PacketInitLogin
import catmoe.fallencrystal.moefilter.network.bungee.limbo.packet.c2s.PacketPluginResponse
import catmoe.fallencrystal.moefilter.network.bungee.limbo.packet.common.PacketKeepAlive
import catmoe.fallencrystal.moefilter.network.bungee.limbo.packet.s2c.*
import java.util.*
import java.util.function.Supplier

@Suppress("ReplaceWithEnumMap")
enum class HandshakeState(private var stateId: Int) {
    HANDSHAKING(0) {
        init {
            serverBound.register({ PacketHandshake() }, map(0x00, min, max))
        }
    },
    STATUS(1),
    LOGIN(2) {
        init {
            serverBound.register({ PacketInitLogin() }, map(0x00, min, max))
            serverBound.register({ PacketPluginResponse() }, map(0x02, min, max))
            clientBound.register({ PacketLoginSuccess() }, map(0x02, min, max))
            clientBound.register({ PacketPluginRequest() }, map(0x04, min, max))
        }
    },
    PLAY(3) {
        init {
            serverBound.register(
                { PacketKeepAlive() },
                map(0x00, Version.V1_7_2, Version.V1_8),
                map(0x0B, Version.V1_9, Version.V1_11_1),
                map(0x0C, Version.V1_12, Version.V1_12),
                map(0x0B, Version.V1_12_1, Version.V1_12_2),
                map(0x0E, Version.V1_13, Version.V1_13_2),
                map(0x0F, Version.V1_14, Version.V1_15_2),
                map(0x10, Version.V1_16, Version.V1_16_4),
                map(0x0F, Version.V1_17, Version.V1_18_2),
                map(0x11, Version.V1_19, Version.V1_19),
                map(0x12, Version.V1_19_1, Version.V1_19_1),
                map(0x11, Version.V1_19_3, Version.V1_19_3),
                map(0x12, Version.V1_19_4, Version.V1_20)
            )
            clientBound.register(
                { PacketJoinGame() },
                map(0x01, Version.V1_7_2, Version.V1_8),
                map(0x23, Version.V1_9, Version.V1_12_2),
                map(0x25, Version.V1_13, Version.V1_14_4),
                map(0x26, Version.V1_15, Version.V1_15_2),
                map(0x25, Version.V1_16, Version.V1_16_1),
                map(0x24, Version.V1_16_2, Version.V1_16_4),
                map(0x26, Version.V1_17, Version.V1_18_2),
                map(0x23, Version.V1_19, Version.V1_19),
                map(0x25, Version.V1_19_1, Version.V1_19_1),
                map(0x24, Version.V1_19_3, Version.V1_19_3),
                map(0x28, Version.V1_19_4, Version.V1_20)
            )
            clientBound.register(
                { PacketPluginMessage() },
                map(0x19, Version.V1_13, Version.V1_13_2),
                map(0x18, Version.V1_14, Version.V1_14_4),
                map(0x19, Version.V1_15, Version.V1_15_2),
                map(0x18, Version.V1_16, Version.V1_16_1),
                map(0x17, Version.V1_16_2, Version.V1_16_4),
                map(0x18, Version.V1_17, Version.V1_18_2),
                map(0x15, Version.V1_19, Version.V1_19),
                map(0x16, Version.V1_19_1, Version.V1_19_1),
                map(0x15, Version.V1_19_3, Version.V1_19_3),
                map(0x17, Version.V1_19_4, Version.V1_20)
            )
            clientBound.register(
                { PacketPlayerAbilities() },
                map(0x39, Version.V1_7_2, Version.V1_8),
                map(0x2B, Version.V1_9, Version.V1_12),
                map(0x2C, Version.V1_12_1, Version.V1_12_2),
                map(0x2E, Version.V1_13, Version.V1_13_2),
                map(0x31, Version.V1_14, Version.V1_14_4),
                map(0x32, Version.V1_15, Version.V1_15_2),
                map(0x31, Version.V1_16, Version.V1_16_1),
                map(0x30, Version.V1_16_2, Version.V1_16_4),
                map(0x32, Version.V1_17, Version.V1_18_2),
                map(0x2F, Version.V1_19, Version.V1_19),
                map(0x31, Version.V1_19_1, Version.V1_19_1),
                map(0x30, Version.V1_19_3, Version.V1_19_3),
                map(0x34, Version.V1_19_4, Version.V1_20)
            )
            clientBound.register(
                { PacketPositionAndLook() },
                map(0x08, Version.V1_7_2, Version.V1_8),
                map(0x2E, Version.V1_9, Version.V1_12),
                map(0x2F, Version.V1_12_1, Version.V1_12_2),
                map(0x32, Version.V1_13, Version.V1_13_2),
                map(0x35, Version.V1_14, Version.V1_14_4),
                map(0x36, Version.V1_15, Version.V1_15_2),
                map(0x35, Version.V1_16, Version.V1_16_1),
                map(0x34, Version.V1_16_2, Version.V1_16_4),
                map(0x38, Version.V1_17, Version.V1_18_2),
                map(0x36, Version.V1_19, Version.V1_19),
                map(0x39, Version.V1_19_1, Version.V1_19_1),
                map(0x38, Version.V1_19_3, Version.V1_19_3),
                map(0x3C, Version.V1_19_4, Version.V1_20)
            )
            clientBound.register(
                { PacketKeepAlive() },
                map(0x00, Version.V1_7_2, Version.V1_8),
                map(0x1F, Version.V1_9, Version.V1_12_2),
                map(0x21, Version.V1_13, Version.V1_13_2),
                map(0x20, Version.V1_14, Version.V1_14_4),
                map(0x21, Version.V1_15, Version.V1_15_2),
                map(0x20, Version.V1_16, Version.V1_16_1),
                map(0x1F, Version.V1_16_2, Version.V1_16_4),
                map(0x21, Version.V1_17, Version.V1_18_2),
                map(0x1E, Version.V1_19, Version.V1_19),
                map(0x20, Version.V1_19_1, Version.V1_19_1),
                map(0x1F, Version.V1_19_3, Version.V1_19_3),
                map(0x23, Version.V1_19_4, Version.V1_20)
            )
            clientBound.register(
                { PacketPlayerInfo() },
                map(0x38, Version.V1_7_2, Version.V1_8),
                map(0x2D, Version.V1_9, Version.V1_12),
                map(0x2E, Version.V1_12_1, Version.V1_12_2),
                map(0x30, Version.V1_13, Version.V1_13_2),
                map(0x33, Version.V1_14, Version.V1_14_4),
                map(0x34, Version.V1_15, Version.V1_15_2),
                map(0x33, Version.V1_16, Version.V1_16_1),
                map(0x32, Version.V1_16_2, Version.V1_16_4),
                map(0x36, Version.V1_17, Version.V1_18_2),
                map(0x34, Version.V1_19, Version.V1_19),
                map(0x37, Version.V1_19_1, Version.V1_19_1),
                map(0x36, Version.V1_19_3, Version.V1_19_3),
                map(0x3A, Version.V1_19_4, Version.V1_20)
            )
            clientBound.register(
                { PacketSpawnPosition() },
                map(0x4C, Version.V1_19_3, Version.V1_19_3),
                map(0x50, Version.V1_19_4, Version.V1_20)
            )
        }
    };

    val serverBound = ProtocolMappings()
    val clientBound = ProtocolMappings()
    fun state(stateId: Int) {
        this.stateId = stateId
    }

    class ProtocolMappings {
        private val registry: MutableMap<Version?, PacketRegistry?> = HashMap()

        fun register(packet: Supplier<*>, vararg mappings: Mapping) {
            for (mapping in mappings) {
                for (ver in getRange(mapping)) {
                    val reg = registry.computeIfAbsent(ver) { version: Version? -> PacketRegistry(version) }
                    reg!!.register(mapping.packetId, packet)
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
                if (curr != null) {
                    curr = curr.prev
                }
            }
            versions.add(from)
            return versions
        }
    }

    class PacketRegistry(val version: Version?) {
        private val packetsById: MutableMap<Int, Supplier<*>> = HashMap()
        private val packetIdByClass: MutableMap<Class<*>, Int> = HashMap()

        fun register(packetId: Int, supplier: Supplier<*>) {
            packetsById[packetId] = supplier
            packetIdByClass[supplier.get().javaClass] = packetId
        }
    }

    class Mapping(val packetId: Int, val from: Version, val to: Version)
    companion object {
        private val STATE_BY_ID: MutableMap<Int, HandshakeState> = HashMap()

        init {
            for (registry in values()) {
                STATE_BY_ID[registry.stateId] = registry
            }
        }

        private fun map(packetId: Int, from: Version, to: Version): Mapping {
            return Mapping(packetId, from, to)
        }
    }
}
