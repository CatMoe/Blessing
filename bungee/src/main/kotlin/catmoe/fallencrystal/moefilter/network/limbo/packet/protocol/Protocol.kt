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

import catmoe.fallencrystal.moefilter.network.limbo.packet.c2s.*
import catmoe.fallencrystal.moefilter.network.limbo.packet.common.*
import catmoe.fallencrystal.moefilter.network.limbo.packet.s2c.*
import catmoe.fallencrystal.moefilter.network.limbo.protocol.Mapping
import catmoe.fallencrystal.translation.utils.version.Version
import catmoe.fallencrystal.translation.utils.version.Version.*
import catmoe.fallencrystal.translation.utils.version.Version.Companion.max
import catmoe.fallencrystal.translation.utils.version.Version.Companion.min


@Suppress("unused")
enum class Protocol(var stateId: Int) {
    @JvmStatic
    HANDSHAKING(0) {init {
            serverBound.register({ PacketHandshake() }, map(0x00, min, max))
            clientBound.register({ PacketDisconnect() }, map(0x00, min, max))
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
            serverBound.register({ PacketLoginAcknowledged() }, map(0x03, V1_20_2, V1_20_2))
            clientBound.register({ PacketLoginSuccess() }, map(0x02, min, max))
            clientBound.register({ PacketPluginRequest() }, map(0x04, min, max))
            clientBound.register({ PacketDisconnect() }, map(0x00, min, max))
        }
    },
    @JvmStatic
    CONFIGURATION(3) {
        init {
            val min = V1_20_2
            serverBound.register({ PacketClientConfiguration() }, map(0x00, min, max))
            clientBound.register({ PacketDisconnect() }, map(0x01, min, max))
            serverBound.register({ PacketPluginMessage() }, map(0x01, min, max))
            serverBound.register({ PacketKeepAlive() }, map(0x03, min, max))
            clientBound.register({ PacketKeepAlive() }, map(0x03, min, max))
            clientBound.register({ PacketFinishConfiguration() }, map(0x02, min, max))
            serverBound.register({ PacketFinishConfiguration() }, map(0x02, min, max))
            clientBound.register({ RegistryData() }, map(0x05, min, max))
        }
    },
    @JvmStatic
    PLAY(4) {
        init {
            clientBound.register(
                { PacketDisconnect() },
                map(0x40, V1_7_6, V1_8),
                map(0x1A, V1_9, V1_12_2),
                map(0x1B, V1_13, V1_13_2),
                map(0x1A, V1_14, V1_14_4),
                map(0x1B, V1_15, V1_15_2),
                map(0x1A, V1_16, V1_16_1),
                map(0x19, V1_16_2, V1_16_4),
                map(0x1A, V1_17, V1_18_2),
                map(0x17, V1_19, V1_19),
                map(0x19, V1_19_1, V1_19_1),
                map(0x17, V1_19_3, V1_19_3),
                map(0x1A, V1_19_4, V1_20),
                map(0x1B, V1_20_2, V1_20_2)
            )
            serverBound.register(
                { PacketKeepAlive() },
                map(0x00, V1_7_6, V1_8),
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
                map(0x12, V1_19_4, V1_20),
                map(0x14, V1_20_2, V1_20_2)
            )
            clientBound.register(
                { PacketKeepAlive() },
                map(0x00, V1_7_6, V1_8),
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
                map(0x23, V1_19_4, V1_20),
                map(0x24, V1_20_2, V1_20_2)
            )
            clientBound.register(
                { PacketJoinGame() },
                map(0x01, V1_7_6, V1_8),
                map(0x23, V1_9, V1_12_2),
                map(0x25, V1_13, V1_14_4),
                map(0x26, V1_15, V1_15_2),
                map(0x25, V1_16, V1_16_1),
                map(0x24, V1_16_2, V1_16_4),
                map(0x26, V1_17, V1_18_2),
                map(0x23, V1_19, V1_19),
                map(0x25, V1_19_1, V1_19_1),
                map(0x24, V1_19_3, V1_19_3),
                map(0x28, V1_19_4, V1_20),
                map(0x29, V1_20_2, V1_20_2),
            )
            clientBound.register(
                { PacketPluginMessage() },
                map(0x3F, V1_8, V1_8),
                map(0x18, V1_9, V1_12_2),
                map(0x19, V1_13, V1_13_2),
                map(0x18, V1_14, V1_14_4),
                map(0x19, V1_15, V1_15_2),
                map(0x18, V1_16, V1_16_1),
                map(0x17, V1_16_2, V1_16_4),
                map(0x18, V1_17, V1_18_2),
                map(0x15, V1_19, V1_19),
                map(0x16, V1_19_1, V1_19_1),
                map(0x15, V1_19_3, V1_19_3),
                map(0x17, V1_19_4, V1_20),
                map(0x18, V1_20_2, V1_20_2)
            )
            serverBound.register(
                { PacketPluginMessage() },
                map(0x17, V1_7_6, V1_8),
                map(0x09, V1_9, V1_11_1),
                map(0x0A, V1_12, V1_12),
                map(0x09, V1_12_1, V1_12_2),
                map(0x0A, V1_13, V1_13_2),
                map(0x0B, V1_14, V1_16_4),
                map(0x0A, V1_17, V1_18_2),
                map(0x0C, V1_19, V1_19),
                map(0x0D, V1_19_1, V1_19_1),
                map(0x0C, V1_19_3, V1_19_3),
                map(0x0D, V1_19_4, V1_20),
                map(0x0F, V1_20_2, V1_20_2)
            )
            clientBound.register(
                { PacketPlayerAbilities() },
                map(0x39, V1_7_6, V1_8),
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
                map(0x34, V1_19_4, V1_20),
                map(0x36, V1_20_2, V1_20_2)
            )
            clientBound.register(
                { PacketServerPositionLook() },
                map(0x08, V1_7_6, V1_8),
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
                map(0x3C, V1_19_4, V1_20),
                map(0x3E, V1_20_2, V1_20_2)
            )
            serverBound.register(
                { PacketClientPositionLook() },
                map(0x06, V1_7_6, V1_8),
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
                map(0x15, V1_19_4, V1_20),
                map(0x17, V1_20_2, V1_20_2)
            )
            serverBound.register(
                { PacketClientPosition() },
                map(0x04, V1_7_6, V1_8),
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
                map(0x14, V1_19_4, V1_20),
                map(0x16, V1_20_2, V1_20_2)
            )
            serverBound.register(
                { PacketClientLook() },
                map(0x05, V1_7_6, V1_8),
                map(0x0E, V1_9, V1_11_1),
                map(0x10, V1_12, V1_12),
                map(0x0F, V1_12_1, V1_12_2),
                map(0x12, V1_13, V1_13_2),
                map(0x13, V1_14, V1_15_2),
                map(0x14, V1_16, V1_16_4),
                map(0x13, V1_17, V1_18_2),
                map(0x15, V1_19, V1_19),
                map(0x16, V1_19_1, V1_19_1),
                map(0x15, V1_19_3, V1_19_3),
                map(0x16, V1_19_4, V1_20),
                map(0x18, V1_20_2, V1_20_2)
            )
            clientBound.register(
                { PacketPlayerInfo() },
                map(0x38, V1_7_6, V1_8),
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
                map(0x3A, V1_19_4, V1_20),
                map(0x3C, V1_20_2, V1_20_2)
            )
            clientBound.register(
                { PacketSpawnPosition() },
                map(0x05, V1_7_6, V1_8),
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
                map(0x50, V1_19_4, V1_20),
                map(0x52, V1_20_2, V1_20_2)
            )
            clientBound.register(
                { PacketEmptyChunk() },
                map(0x21, V1_7_6, V1_8),
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
                map(0x24, V1_19_4, V1_20),
                map(0x25, V1_20_2, V1_20_2)
            )
            clientBound.register(
                { PacketSetHeldSlot() },
                map(0x2F, V1_7_6, V1_8),
                map(0x16, V1_9, V1_12_2),
                map(0x17, V1_13, V1_13_2),
                map(0x16, V1_14, V1_14_4),
                map(0x17, V1_15, V1_15_2),
                map(0x16, V1_16, V1_16_2),
                map(0x15, V1_16_3, V1_16_4),
                map(0x16, V1_17, V1_18_2),
                map(0x13, V1_19, V1_19_1),
                map(0x12, V1_19_3, V1_19_3),
                map(0x14, V1_19_4, V1_20),
                map(0x15, V1_20_2, V1_20_2)
            )
            clientBound.register(
                { PacketSetExperience() },
                map(0x1F, V1_7_6, V1_8),
                map(0x3D, V1_9, V1_11_1),
                map(0x3F, V1_12, V1_12),
                map(0x40, V1_12_1, V1_12_2),
                map(0x43, V1_13, V1_13_2),
                map(0x47, V1_14, V1_14_4),
                map(0x48, V1_15, V1_16_4),
                map(0x51, V1_17, V1_19),
                map(0x54, V1_19_1, V1_19_1),
                map(0x52, V1_19_3, V1_19_3),
                map(0x56, V1_19_4, V1_20_2)
            )
            clientBound.register(
                { PacketUpdateTime() },
                map(0x03, V1_7_6, V1_8),
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
                map(0x5E, V1_19_4, V1_20),
                map(0x60, V1_20_2, V1_20_2)
            )
            serverBound.register(
                { PacketClientChat() },
                map(0x01, V1_7_6, V1_8),
                map(0x02, V1_9, V1_11_1),
                map(0x03, V1_12, V1_12),
                map(0x02, V1_12_1, V1_13_2),
                map(0x03, V1_14, V1_18_2),
                map(0x04, V1_19, V1_19),
                map(0x05, V1_19_1, V1_20_2)
            )
            clientBound.register(
                { PacketTransaction() },
                map(0x32, V1_7_6, V1_8),
                map(0x11, V1_9, V1_12_2),
                map(0x12, V1_13, V1_14_4),
                map(0x13, V1_15, V1_15_2),
                map(0x12, V1_16, V1_16_1),
                map(0x11, V1_16_2, V1_16_4),
                map(0x30, V1_17, V1_18_2),
                map(0x2D, V1_19, V1_19),
                map(0x2F, V1_19_1, V1_19_1),
                map(0x2E, V1_19_3, V1_19_3),
                map(0x32, V1_19_4, V1_20),
                map(0x33, V1_20_2, V1_20_2)
            )
            serverBound.register(
                { PacketTransaction() },
                map(0x0F, V1_7_6, V1_8),
                map(0x05, V1_9, V1_11_1),
                map(0x06, V1_12, V1_12),
                map(0x05, V1_12_1, V1_12_2),
                map(0x06, V1_13, V1_13_2),
                map(0x07, V1_14, V1_16_4),
                map(0x1D, V1_17, V1_18_2),
                map(0x1F, V1_19, V1_19),
                map(0x20, V1_19_1, V1_19_1),
                map(0x1F, V1_19_3, V1_19_3),
                map(0x20, V1_19_4, V1_20),
                map(0x23, V1_20_2, V1_20_2)
            )
            clientBound.register(
                { PacketBlocksSectionUpdate() },
                map(0x22, V1_7_6, V1_8),
                map(0x10, V1_9, V1_12_2),
                map(0x0F, V1_13, V1_14_4),
                map(0x10, V1_15, V1_15_2),
                map(0x0F, V1_16, V1_16_2),
                map(0x3B, V1_16_2, V1_16_4),
                map(0x3F, V1_17, V1_18_2),
                map(0x3D, V1_19, V1_19),
                map(0x40, V1_19_1, V1_19_1),
                map(0x3F, V1_19_3, V1_19_3),
                map(0x43, V1_19_4, V1_20),
                map(0x45, V1_20_2, V1_20_2)
            )
            clientBound.register(
                {
                    @Suppress("DEPRECATION")
                    PacketBlockUpdate()
                },
                map(0x23, V1_7_6, V1_8),
                map(0x0B, V1_9, V1_14_4),
                map(0x0C, V1_15, V1_15_2),
                map(0x0B, V1_16, V1_16_4),
                map(0x0C, V1_17, V1_18_2),
                map(0x09, V1_19, V1_19_3),
                map(0x1A, V1_19_4, V1_20),
                map(0x09, V1_20_2, V1_20_2)
            )
            serverBound.register(
                { PacketTeleportConfirm() },
                map(0x00, V1_9, V1_20_2)
            )
            /*
                        serverBound.register(
                { TabComplete() },
                map(0x14, V1_7_6, V1_8),
                map(0x01, V1_9, V1_11_1),
                map(0x02, V1_12, V1_12),
                map(0x01, V1_12_1, V1_12_2),
                map(0x5, V1_13, V1_13_2),
                map(0x06, V1_14, V1_18_2),
                map(0x08, V1_19, V1_19),
                map(0x09, V1_19_1, V1_19_1),
                map(0x08, V1_19_3, V1_19_3),
                map(0x09, V1_19_4, V1_20)
            )
            clientBound.register(
                { TabComplete() },
                map(0x3A, V1_7_6, V1_8),
                map(0x0E, V1_9, V1_12_2),
                map(0x10, V1_13, V1_14_4),
                map(0x11, V1_15, V1_15_2),
                map(0x10, V1_16, V1_16_1),
                map(0x0F, V1_16_2, V1_16_4),
                map(0x11, V1_17, V1_18_2),
                map(0x0E, V1_19, V1_19_1),
                map(0x0D, V1_19_3, V1_19_3),
                map(0xF, V1_19_4, V1_20)
            )
             */
        }
    };

    fun map(packetId: Int, from: Version, to: Version): Mapping { return Mapping(packetId, from, to) }


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
