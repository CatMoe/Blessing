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

import catmoe.fallencrystal.moefilter.network.limbo.packet.c2s.*
import catmoe.fallencrystal.moefilter.network.limbo.packet.common.PacketKeepAlive
import catmoe.fallencrystal.moefilter.network.limbo.packet.common.PacketStatusPing
import catmoe.fallencrystal.moefilter.network.limbo.packet.s2c.*
import catmoe.fallencrystal.moefilter.network.limbo.util.Version
import catmoe.fallencrystal.moefilter.network.limbo.util.Version.*
import catmoe.fallencrystal.moefilter.network.limbo.util.Version.Companion.max
import catmoe.fallencrystal.moefilter.network.limbo.util.Version.Companion.min


@Suppress("unused")
enum class Protocol(var stateId: Int) {
    @JvmStatic
    HANDSHAKING(0) {
        init {
            serverBound.register({ PacketHandshake() }, map(0x00, min, max))
        }
    },
    @JvmStatic
    STATUS(1) {
       init {
           serverBound.register({ PacketStatusRequest() }, map(0x00, min, max))
           clientBound.register({ PacketPingResponse() }, map(0x00, min, max))
           serverBound.register({ PacketStatusPing() }, map(0x01, min, max))
           clientBound.register({ PacketStatusPing() }, map(0x01, min, max))
       }
    },
    @JvmStatic
    LOGIN(2) {
        init {
            serverBound.register({ PacketInitLogin() }, map(0x00, min, max))
            serverBound.register({ PacketPluginResponse() }, map(0x02, min, max))
            clientBound.register({ PacketLoginSuccess() }, map(0x02, min, max))
            clientBound.register({ PacketPluginRequest() }, map(0x04, min, max))
        }
    },
    @JvmStatic
    PLAY(3) {
        init {
            serverBound.register(
                { PacketKeepAlive() },
                map(0x00, V1_7_2, V1_8),
                map(0x0B, V1_9, V1_11_1),
                map(0x0C, V1_12, V1_12),
                map(0x0B, V1_12_1, V1_12_2),
                map(0x0E, V1_13, V1_13_2),
                map(0x0F, V1_14, V1_15_2),
                map(0x10, V1_16, V1_16_4),
                map(0x0F, V1_17, V1_18_2),
                map(0x11, V1_19, V1_19),
                map(0x12, V1_19_1, V1_19_1),
                map(0x11, V1_19_3, V1_19_3),
                map(0x12, V1_19_4, V1_20)
            )
            clientBound.register(
                { PacketKeepAlive() },
                map(0x00, V1_7_2, V1_8),
                map(0x1F, V1_9, V1_12_2),
                map(0x21, V1_13, V1_13_2),
                map(0x20, V1_14, V1_14_4),
                map(0x21, V1_15, V1_15_2),
                map(0x20, V1_16, V1_16_1),
                map(0x1F, V1_16_2, V1_16_4),
                map(0x21, V1_17, V1_18_2),
                map(0x1E, V1_19, V1_19),
                map(0x20, V1_19_1, V1_19_1),
                map(0x1F, V1_19_3, V1_19_3),
                map(0x23, V1_19_4, V1_20)
            )
            clientBound.register(
                { PacketJoinGame() },
                map(0x01, V1_7_2, V1_8),
                map(0x23, V1_9, V1_12_2),
                map(0x25, V1_13, V1_14_4),
                map(0x26, V1_15, V1_15_2),
                map(0x25, V1_16, V1_16_1),
                map(0x24, V1_16_2, V1_16_4),
                map(0x26, V1_17, V1_18_2),
                map(0x23, V1_19, V1_19),
                map(0x25, V1_19_1, V1_19_1),
                map(0x24, V1_19_3, V1_19_3),
                map(0x28, V1_19_4, V1_20)
            )
            clientBound.register(
                { PacketPluginMessage() },
                map(0x19, V1_13, V1_13_2),
                map(0x18, V1_14, V1_14_4),
                map(0x19, V1_15, V1_15_2),
                map(0x18, V1_16, V1_16_1),
                map(0x17, V1_16_2, V1_16_4),
                map(0x18, V1_17, V1_18_2),
                map(0x15, V1_19, V1_19),
                map(0x16, V1_19_1, V1_19_1),
                map(0x15, V1_19_3, V1_19_3),
                map(0x17, V1_19_4, V1_20)
            )
            clientBound.register(
                { PacketPlayerAbilities() },
                map(0x39, V1_7_2, V1_8),
                map(0x2B, V1_9, V1_12),
                map(0x2C, V1_12_1, V1_12_2),
                map(0x2E, V1_13, V1_13_2),
                map(0x31, V1_14, V1_14_4),
                map(0x32, V1_15, V1_15_2),
                map(0x31, V1_16, V1_16_1),
                map(0x30, V1_16_2, V1_16_4),
                map(0x32, V1_17, V1_18_2),
                map(0x2F, V1_19, V1_19),
                map(0x31, V1_19_1, V1_19_1),
                map(0x30, V1_19_3, V1_19_3),
                map(0x34, V1_19_4, V1_20)
            )
            clientBound.register(
                { PacketServerPositionLook() },
                map(0x08, V1_7_2, V1_8),
                map(0x2E, V1_9, V1_12),
                map(0x2F, V1_12_1, V1_12_2),
                map(0x32, V1_13, V1_13_2),
                map(0x35, V1_14, V1_14_4),
                map(0x36, V1_15, V1_15_2),
                map(0x35, V1_16, V1_16_1),
                map(0x34, V1_16_2, V1_16_4),
                map(0x38, V1_17, V1_18_2),
                map(0x36, V1_19, V1_19),
                map(0x39, V1_19_1, V1_19_1),
                map(0x38, V1_19_3, V1_19_3),
                map(0x3C, V1_19_4, V1_20)
            )
            serverBound.register(
                { PacketClientPositionLook() },
                map(0x08, V1_7_2, V1_7_6),
                map(0x06, V1_8, V1_8),
                map(0x0D, V1_9, V1_11_1),
                map(0x0F, V1_12, V1_12),
                map(0x0E, V1_12_1, V1_12_2),
                map(0x11, V1_13, V1_13_2),
                map(0x12, V1_14, V1_15_2),
                map(0x13, V1_16, V1_16_4),
                map(0x12, V1_17, V1_18_2),
                map(0x14, V1_19, V1_19),
                map(0x15, V1_19_1, V1_19_1),
                map(0x14, V1_19_3, V1_19_3),
                map(0x15, V1_19_4, V1_20)
            )
            serverBound.register(
                { PacketClientPosition() },
                map(0x04, V1_7_2, V1_8),
                map(0x0C, V1_9, V1_11_1),
                map(0x0E, V1_12, V1_12),
                map(0X0D, V1_12_1, V1_12_2),
                map(0x10, V1_13, V1_13_2),
                map(0x11, V1_14, V1_15_2),
                map(0x12, V1_16, V1_16_4),
                map(0x11, V1_17, V1_18_2),
                map(0x13, V1_19, V1_19),
                map(0x14, V1_19_1, V1_19_1),
                map(0x13, V1_19_3, V1_19_3),
                map(0x14, V1_19_4, V1_20)
            )
            serverBound.register(
                { PacketClientLook() },
                map(0x05, V1_7_2, V1_7_6),
                map(0x03, V1_8, V1_8),
                map(0x0E, V1_9, V1_11_1),
                map(0x10, V1_12, V1_12),
                map(0x0F, V1_12_1, V1_12_2),
                map(0x12, V1_13, V1_13_2),
                map(0x13, V1_14, V1_14_3),
                map(0x14, V1_14_4, V1_14_4)
                // Unsupported on 1.15
            )
            clientBound.register(
                { PacketPlayerInfo() },
                map(0x38, V1_7_2, V1_8),
                map(0x2D, V1_9, V1_12),
                map(0x2E, V1_12_1, V1_12_2),
                map(0x30, V1_13, V1_13_2),
                map(0x33, V1_14, V1_14_4),
                map(0x34, V1_15, V1_15_2),
                map(0x33, V1_16, V1_16_1),
                map(0x32, V1_16_2, V1_16_4),
                map(0x36, V1_17, V1_18_2),
                map(0x34, V1_19, V1_19),
                map(0x37, V1_19_1, V1_19_1),
                map(0x36, V1_19_3, V1_19_3),
                map(0x3A, V1_19_4, V1_20)
            )
            clientBound.register(
                { PacketSpawnPosition() },
                map(0x05, V1_7_2, V1_8),
                map(0x43, V1_9, V1_11_1),
                map(0x45, V1_12, V1_12),
                map(0x46, V1_12_1, V1_12_2),
                map(0x49, V1_13, V1_13_2),
                map(0x4D, V1_14, V1_14_4),
                map(0x4E, V1_15, V1_15_2),
                map(0x42, V1_16, V1_16_4),
                map(0x4B, V1_17, V1_18_2),
                map(0x4A, V1_19, V1_19),
                map(0x4D, V1_19_1, V1_19_1),
                map(0x4C, V1_19_3, V1_19_3),
                map(0x50, V1_19_4, V1_20)
            )
            clientBound.register(
                { PacketEmptyChunk() },
                map(0x21, V1_7_2, V1_8),
                map(0x20, V1_9, V1_12_2),
                map(0x22, V1_13, V1_13_2),
                map(0x21, V1_14, V1_14_4),
                map(0x22, V1_15, V1_15_2),
                map(0x21, V1_16, V1_16_1),
                map(0x20, V1_16_2, V1_16_4),
                map(0x22, V1_17, V1_18_2),
                map(0x1F, V1_19, V1_19),
                map(0x21, V1_19_1, V1_19_1),
                map(0x20, V1_19_3, V1_19_3),
                map(0x24, V1_19_4, V1_20)
            )
            clientBound.register(
                { PacketSetHeldSlot() },
                map(0x2F, V1_7_2, V1_8),
                map(0x16, V1_9, V1_12_2),
                map(0x17, V1_13, V1_13_2),
                map(0x16, V1_14, V1_14_4),
                map(0x17, V1_15, V1_15_2),
                map(0x16, V1_16, V1_16_2),
                map(0x15, V1_16_3, V1_16_4),
                map(0x16, V1_17, V1_18_2),
                map(0x13, V1_19, V1_19_1),
                map(0x12, V1_19_3, V1_19_3),
                map(0x14, V1_19_4, V1_20)
            )
            clientBound.register(
                { PacketSetExperience() },
                map(0x1F, V1_7_2, V1_8),
                map(0x3D, V1_9, V1_11_1),
                map(0x3F, V1_12, V1_12),
                map(0x40, V1_12_1, V1_12_2),
                map(0x43, V1_13, V1_13_2),
                map(0x47, V1_14, V1_14_4),
                map(0x48, V1_15, V1_16_4),
                map(0x51, V1_17, V1_19),
                map(0x54, V1_19_1, V1_19_1),
                map(0x52, V1_19_3, V1_19_3),
                map(0x56, V1_19_4, V1_20)
            )
            clientBound.register(
                { PacketUpdateTime() },
                map(0x03, V1_7_2, V1_8),
                map(0x44, V1_9, V1_11_1),
                map(0x46, V1_12, V1_12),
                map(0x47, V1_12_1, V1_12_2),
                map(0x4A, V1_13, V1_13_2),
                map(0x4E, V1_14, V1_14_4),
                map(0x4F, V1_15, V1_15_2),
                map(0x4E, V1_16, V1_16_4),
                map(0x58, V1_17, V1_17_1),
                map(0x59, V1_18, V1_19),
                map(0x5C, V1_19_1, V1_19_1),
                map(0x5A, V1_19_3, V1_19_3),
                map(0x5E, V1_19_4, V1_20)
            )
        }
    };

    fun map(packetId: Int, from: Version, to: Version): Mapping {
        return Mapping(packetId, from, to)
    }


    val serverBound = ProtocolMappings()
    val clientBound = ProtocolMappings()
    fun state(stateId: Int) { this.stateId = stateId }


    companion object {
        val STATE_BY_ID: MutableMap<Int, Protocol> = HashMap()

        /*
        init {
            for (registry in values()) {
                STATE_BY_ID[registry.stateId] = registry
            }
        }
         */
    }
}
