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

package catmoe.fallencrystal.moefilter.network.limbo.packet.s2c

import catmoe.fallencrystal.moefilter.network.limbo.dimension.DimensionInterface.ADVENTURE
import catmoe.fallencrystal.moefilter.network.limbo.dimension.DimensionInterface.LLBIT
import catmoe.fallencrystal.moefilter.network.limbo.dimension.adventure.DimensionRegistry.codec_1_16
import catmoe.fallencrystal.moefilter.network.limbo.dimension.adventure.DimensionRegistry.codec_1_18_2
import catmoe.fallencrystal.moefilter.network.limbo.dimension.adventure.DimensionRegistry.codec_1_19
import catmoe.fallencrystal.moefilter.network.limbo.dimension.adventure.DimensionRegistry.codec_1_19_1
import catmoe.fallencrystal.moefilter.network.limbo.dimension.adventure.DimensionRegistry.codec_1_19_4
import catmoe.fallencrystal.moefilter.network.limbo.dimension.adventure.DimensionRegistry.codec_1_20
import catmoe.fallencrystal.moefilter.network.limbo.dimension.adventure.DimensionRegistry.codec_Legacy
import catmoe.fallencrystal.moefilter.network.limbo.dimension.adventure.DimensionRegistry.defaultDimension1_16
import catmoe.fallencrystal.moefilter.network.limbo.dimension.adventure.DimensionRegistry.defaultDimension1_18_2
import catmoe.fallencrystal.moefilter.network.limbo.dimension.llbit.StaticDimension
import catmoe.fallencrystal.moefilter.network.limbo.handler.MoeLimbo
import catmoe.fallencrystal.moefilter.network.limbo.netty.ByteMessage
import catmoe.fallencrystal.moefilter.network.limbo.packet.LimboS2CPacket
import catmoe.fallencrystal.moefilter.network.limbo.util.Version
import catmoe.fallencrystal.moefilter.network.limbo.util.Version.*


@Suppress("MemberVisibilityCanBePrivate")
class PacketJoinGame : LimboS2CPacket() {

    var entityId = 0
    var isHardcore = true
    var gameMode = 2
    var previousGameMode = -1
    var worldName: String = when (MoeLimbo.dimLoaderMode) {
        ADVENTURE -> catmoe.fallencrystal.moefilter.network.limbo.dimension.adventure.DimensionType.OVERWORLD.dimensionName
        LLBIT -> catmoe.fallencrystal.moefilter.network.limbo.dimension.llbit.DimensionType.OVERWORLD.dimension.key
    }
    var worldNames: Array<String?> = arrayOf(worldName)
    var hashedSeed: Long = 0
    var maxPlayers = 1
    var viewDistance = 2
    var reducedDebugInfo = true
    var enableRespawnScreen = true
    var isDebug = false
    var isFlat = true

    override fun encode(packet: ByteMessage, version: Version?) {
        when (MoeLimbo.dimLoaderMode) {
            ADVENTURE -> encodeAdventure(packet, version!!)
            LLBIT -> encodeLLBIT(packet, version!!)
        }
    }

    private fun encodeLLBIT(packet: ByteMessage, version: Version) {
        packet.writeInt(entityId)
        val dim = StaticDimension.dim.dimension
        val tag = StaticDimension.cacheDimension.getIfPresent(version)!!
        if (version.moreOrEqual(V1_16_2)) packet.writeBoolean(isHardcore)
        // Hardcore
        if (version.moreOrEqual(V1_16_2)) packet.writeBoolean(isHardcore)
        // Game mode
        if (version.fromTo(V1_7_2, V1_7_6)) packet.writeByte(if (gameMode == 3) 1 else gameMode) else packet.writeByte(gameMode)
        if (version.moreOrEqual(V1_16)) {
            packet.writeByte(previousGameMode)
            packet.writeStringsArray(worldNames)
            packet.writeTag(tag)
            if (version.moreOrEqual(V1_19) || version.fromTo(V1_16, V1_16_1))
                packet.writeString(worldName)
            else packet.writeTag(dim.getAttributes(version))
            packet.writeString(worldName)
        }
        if (version.fromTo(V1_7_2, V1_9)) { packet.writeByte(dim.dimensionId) }
        else if (version.fromTo(V1_9_1, V1_15_2)) packet.writeInt(dim.dimensionId)
        if (version.moreOrEqual(V1_15)) packet.writeLong(hashedSeed)
        if (version.fromTo(V1_7_2, V1_13_2)) packet.writeByte(0) // Difficulty
        if (version.moreOrEqual(V1_16_2)) packet.writeVarInt(maxPlayers) else packet.writeByte(maxPlayers)
        if (version.fromTo(V1_7_2, V1_15_2)) packet.writeString("flat")
        if (version.moreOrEqual(V1_7_2)) packet.writeVarInt(viewDistance)
        if (version.moreOrEqual(V1_18)) packet.writeVarInt(viewDistance)
        if (version.moreOrEqual(V1_8)) packet.writeBoolean(reducedDebugInfo)
        if (version.moreOrEqual(V1_15)) packet.writeBoolean(enableRespawnScreen)
        if (version.moreOrEqual(V1_16)) {
            packet.writeBoolean(isDebug)
            packet.writeBoolean(isFlat)
        }
        if (version.moreOrEqual(V1_19)) {
            packet.writeBoolean(false)
            if (version.moreOrEqual(V1_20)) packet.writeVarInt(0)
        }
    }

    private fun encodeAdventure(packet: ByteMessage, version: Version) {
        packet.writeInt(entityId)

        // Hardcore
        if (version.moreOrEqual(V1_16_2)) packet.writeBoolean(isHardcore)

        // Game mode
        if (version.fromTo(V1_7_2, V1_7_6)) packet.writeByte(if (gameMode == 3) 1 else gameMode)
        else packet.writeByte(gameMode)

        // Previous game mode & world names
        if (version.moreOrEqual(V1_16)) {
            packet.writeByte(previousGameMode)

            /*
            Write world(s) names

            In ByteMessage.kt line 119: (WriteStringsArray)
            writeVarInt(array.size)
            array.forEach { writeString(it) }
             */
            packet.writeStringsArray(worldNames)
        }

        // Dimension
        if (version.fromTo(V1_7_2, V1_9)) packet.writeByte(defaultDimension1_16.id)
        else if (version.fromTo(V1_9_1, V1_15_2)) packet.writeInt(defaultDimension1_16.id)
        else if (version.fromTo(V1_16, V1_16_1)) {
            packet.writeCompoundTag(codec_Legacy)
            packet.writeString(defaultDimension1_16.name)
        } else if (version.fromTo(V1_16_2, V1_18)) {
            packet.writeCompoundTag(codec_1_16)
            packet.writeCompoundTag(defaultDimension1_16.data)
        } else if (version == V1_18_2) {
            packet.writeCompoundTag(codec_1_18_2)
            packet.writeCompoundTag(defaultDimension1_18_2.data)
        } else if (version == V1_19) packet.writeCompoundTag(codec_1_19)
        else if (version.fromTo(V1_19_1, V1_19_3)) packet.writeCompoundTag(codec_1_19_1)
        else if (version == V1_19_4) packet.writeCompoundTag(codec_1_19_4)
        else packet.writeCompoundTag(codec_1_20)

        // World name
        if (version.moreOrEqual(V1_16)) {
            if (version.moreOrEqual(V1_19)) packet.writeString(worldName) // World type
            packet.writeString(worldName)
        }

        // Hashed seed
        if (version.moreOrEqual(V1_15)) packet.writeLong(hashedSeed)

        // Legacy difficulty & maxPlayers
        if (version.fromTo(V1_7_2, V1_13_2)) packet.writeByte(0) // Difficulty
        if (version.moreOrEqual(V1_16_2)) packet.writeVarInt(maxPlayers) else packet.writeByte(maxPlayers)

        // Legacy level type
        if (version.fromTo(V1_7_2, V1_15_2)) packet.writeString("flat")

        // View distance
        if (version.moreOrEqual(V1_14)) packet.writeVarInt(viewDistance)
        if (version.moreOrEqual(V1_18)) packet.writeVarInt(viewDistance) // Simulation Distance

        // reducedDebugInfo && enableRespawnScreen && isDebug && isFlat
        if (version.moreOrEqual(V1_8)) packet.writeBoolean(reducedDebugInfo)
        if (version.moreOrEqual(V1_15)) packet.writeBoolean(enableRespawnScreen)
        if (version.moreOrEqual(V1_16)) {
            packet.writeBoolean(isDebug)
            packet.writeBoolean(isFlat)
        }
        if (version.moreOrEqual(V1_19)) {
            packet.writeBoolean(false) // lastDeathPos
            if (version.moreOrEqual(V1_20)) packet.writeVarInt(0) // Pearl cool down
        }

        // Legacy
        /*
        packet.writeInt(entityId)

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
         */
    }

    override fun toString(): String {
        return "" +
                "entityId=$entityId," +
                "isHardcore=$isHardcore," +
                "gameMode=$gameMode," +
                "previousGameMode=$previousGameMode," +
                "worldNames=$worldNames," +
                "worldName=$worldName," +
                "hashedSeed=$hashedSeed," +
                "maxPlayers=$maxPlayers," +
                "viewDistance=$viewDistance," +
                "reducedDebugInfo=$reducedDebugInfo," +
                "enableRespawnScreen=$enableRespawnScreen," +
                "isDebug=$isDebug," +
                "isFlat=$isFlat"
    }
}