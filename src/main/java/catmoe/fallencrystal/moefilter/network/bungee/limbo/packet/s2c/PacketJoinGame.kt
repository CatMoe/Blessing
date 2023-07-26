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

package catmoe.fallencrystal.moefilter.network.bungee.limbo.packet.s2c

import catmoe.fallencrystal.moefilter.network.bungee.limbo.dimension.DimensionRegistry
import catmoe.fallencrystal.moefilter.network.bungee.limbo.dimension.DimensionType
import catmoe.fallencrystal.moefilter.network.bungee.limbo.packet.ByteMessage
import catmoe.fallencrystal.moefilter.network.bungee.limbo.packet.LimboS2CPacket
import catmoe.fallencrystal.moefilter.network.bungee.limbo.util.Version


@Suppress("unused", "MemberVisibilityCanBePrivate")
class PacketJoinGame : LimboS2CPacket() {

    var entityId = 0
    var isHardcore = true
    var gameMode = 2
    var previousGameMode = -1
    val worldNames: Array<String?>? = null
    var worldName: String = DimensionType.OVERWORLD.dimensionName
    var hashedSeed: Long = 0
    var maxPlayers = 1
    var viewDistance = 2
    var reducedDebugInfo = true
    var enableRespawnScreen = true
    var isDebug = false
    var isFlat = true

    override fun encode(packet: ByteMessage, version: Version?) {
        packet.writeInt(entityId)

        val worldNames: Array<String?> = this.worldNames ?: arrayOf(
            DimensionType.OVERWORLD.dimensionName,
            DimensionType.THE_NETHER.dimensionName,
            DimensionType.THE_END.dimensionName
        )

        if (version!!.fromTo(Version.V1_7_2, Version.V1_7_6)) {
            packet.writeByte(if (gameMode == 3) 1 else gameMode)
            packet.writeByte(DimensionRegistry.defaultDimension1_16.id)
            packet.writeByte(0) // Difficulty
            packet.writeByte(maxPlayers)
            packet.writeString("flat") // Level type
        }

        if (version.fromTo(Version.V1_8, Version.V1_9)) {
            packet.writeByte(gameMode)
            packet.writeByte(DimensionRegistry.defaultDimension1_16.id)
            packet.writeByte(0) // Difficulty
            packet.writeByte(maxPlayers)
            packet.writeString("flat") // Level type
            packet.writeBoolean(reducedDebugInfo)
        }

        if (version.fromTo(Version.V1_9_1, Version.V1_13_2)) {
            packet.writeByte(gameMode)
            packet.writeInt(DimensionRegistry.defaultDimension1_16.id)
            packet.writeByte(0) // Difficulty
            packet.writeByte(maxPlayers)
            packet.writeString("flat") // Level type
            packet.writeBoolean(reducedDebugInfo)
        }

        if (version.fromTo(Version.V1_14, Version.V1_14_4)) {
            packet.writeByte(gameMode)
            packet.writeInt(DimensionRegistry.defaultDimension1_16.id)
            packet.writeByte(maxPlayers)
            packet.writeString("flat") // Level type
            packet.writeVarInt(viewDistance)
            packet.writeBoolean(reducedDebugInfo)
        }

        if (version.fromTo(Version.V1_15, Version.V1_15_2)) {
            packet.writeByte(gameMode)
            packet.writeInt(DimensionRegistry.defaultDimension1_16.id)
            packet.writeLong(hashedSeed)
            packet.writeByte(maxPlayers)
            packet.writeString("flat") // Level type
            packet.writeVarInt(viewDistance)
            packet.writeBoolean(reducedDebugInfo)
            packet.writeBoolean(enableRespawnScreen)
        }

        if (version.fromTo(Version.V1_16, Version.V1_16_1)) {
            packet.writeByte(gameMode)
            packet.writeByte(previousGameMode)
            packet.writeStringsArray(worldNames)
            packet.writeCompoundTag(DimensionRegistry.codec_Legacy)
            packet.writeString(DimensionRegistry.defaultDimension1_16.name)
            packet.writeString(worldName)
            packet.writeLong(hashedSeed)
            packet.writeByte(maxPlayers)
            packet.writeVarInt(viewDistance)
            packet.writeBoolean(reducedDebugInfo)
            packet.writeBoolean(enableRespawnScreen)
            packet.writeBoolean(isDebug)
            packet.writeBoolean(isFlat)
        }

        if (version.fromTo(Version.V1_16_2, Version.V1_17_1)) {
            packet.writeBoolean(isHardcore)
            packet.writeByte(gameMode)
            packet.writeByte(previousGameMode)
            packet.writeStringsArray(worldNames)
            packet.writeCompoundTag(DimensionRegistry.codec_1_16)
            packet.writeCompoundTag(DimensionRegistry.defaultDimension1_16.data)
            packet.writeString(worldName)
            packet.writeLong(hashedSeed)
            packet.writeVarInt(maxPlayers)
            packet.writeVarInt(viewDistance)
            packet.writeBoolean(reducedDebugInfo)
            packet.writeBoolean(enableRespawnScreen)
            packet.writeBoolean(isDebug)
            packet.writeBoolean(isFlat)
        }

        if (version.fromTo(Version.V1_18, Version.V1_18_2)) {
            packet.writeBoolean(isHardcore)
            packet.writeByte(gameMode)
            packet.writeByte(previousGameMode)
            packet.writeStringsArray(worldNames)
            if (version.moreOrEqual(Version.V1_18_2)) {
                packet.writeCompoundTag(DimensionRegistry.codec_1_18_2)
                packet.writeCompoundTag(DimensionRegistry.defaultDimension1_18_2.data)
            } else {
                packet.writeCompoundTag(DimensionRegistry.codec_1_16)
                packet.writeCompoundTag(DimensionRegistry.defaultDimension1_16.data)
            }
            packet.writeString(worldName)
            packet.writeLong(hashedSeed)
            packet.writeVarInt(maxPlayers)
            packet.writeVarInt(viewDistance)
            packet.writeVarInt(viewDistance) // Simulation Distance
            packet.writeBoolean(reducedDebugInfo)
            packet.writeBoolean(enableRespawnScreen)
            packet.writeBoolean(isDebug)
            packet.writeBoolean(isFlat)
        }

        if (version.fromTo(Version.V1_19, Version.V1_19_4)) {
            packet.writeBoolean(isHardcore)
            packet.writeByte(gameMode)
            packet.writeByte(previousGameMode)
            packet.writeStringsArray(worldNames)
            if (version.moreOrEqual(Version.V1_19_1)) {
                if (version.moreOrEqual(Version.V1_19_4)) {
                    packet.writeCompoundTag(DimensionRegistry.codec_1_19_4)
                } else {
                    packet.writeCompoundTag(DimensionRegistry.codec_1_19_1)
                }
            } else {
                packet.writeCompoundTag(DimensionRegistry.codec_1_19)
            }
            packet.writeString(worldName) // World type
            packet.writeString(worldName)
            packet.writeLong(hashedSeed)
            packet.writeVarInt(maxPlayers)
            packet.writeVarInt(viewDistance)
            packet.writeVarInt(viewDistance) // Simulation Distance
            packet.writeBoolean(reducedDebugInfo)
            packet.writeBoolean(enableRespawnScreen)
            packet.writeBoolean(isDebug)
            packet.writeBoolean(isFlat)
            packet.writeBoolean(false)
        }

        if (version.moreOrEqual(Version.V1_20)) {
            packet.writeBoolean(isHardcore)
            packet.writeByte(gameMode)
            packet.writeByte(previousGameMode)
            packet.writeStringsArray(worldNames)
            packet.writeCompoundTag(DimensionRegistry.codec_1_20)
            packet.writeString(worldName) // World type
            packet.writeString(worldName)
            packet.writeLong(hashedSeed)
            packet.writeVarInt(maxPlayers)
            packet.writeVarInt(viewDistance)
            packet.writeVarInt(viewDistance) // Simulation Distance
            packet.writeBoolean(reducedDebugInfo)
            packet.writeBoolean(enableRespawnScreen)
            packet.writeBoolean(isDebug)
            packet.writeBoolean(isFlat)
            packet.writeBoolean(false)
            packet.writeVarInt(0)
        }
    }
}