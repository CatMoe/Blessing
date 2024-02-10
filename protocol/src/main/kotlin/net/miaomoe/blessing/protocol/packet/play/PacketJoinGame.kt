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

package net.miaomoe.blessing.protocol.packet.play

import net.kyori.adventure.nbt.CompoundBinaryTag
import net.miaomoe.blessing.nbt.dimension.Dimension
import net.miaomoe.blessing.nbt.dimension.NbtVersion
import net.miaomoe.blessing.nbt.dimension.World
import net.miaomoe.blessing.protocol.direction.PacketDirection
import net.miaomoe.blessing.protocol.packet.type.PacketBidirectional
import net.miaomoe.blessing.protocol.util.ByteMessage
import net.miaomoe.blessing.protocol.version.Version

@Suppress("MemberVisibilityCanBePrivate")
class PacketJoinGame(
    var entityId: Int = -1,
    var gameMode: Int = 0,
    var hashedSeed: Long = 0,
    var isHardcore: Boolean = false,
    var viewDistance: Int = 4,
    var reducedDebugInfo: Boolean = false,
    var showRespawnScreen: Boolean = false,
    var limitedCrafting: Boolean = true,
    var dimension: Dimension = World.OVERWORLD.dimension,
    var levelName: String = dimension.key,
    var levelNames: Array<String?> = arrayOf(levelName)
) : PacketBidirectional {

    override val forceDirection = PacketDirection.TO_CLIENT

    override fun encode(byteBuf: ByteMessage, version: Version, direction: PacketDirection) {
        byteBuf.writeInt(entityId)
        when {
            version.fromTo(Version.V1_7_2, Version.V1_13_2) ->
                write17(
                    byteBuf,
                    v18 = version.moreOrEqual(Version.V1_8),
                    v19 = version.moreOrEqual(Version.V1_9_1)
                )
            version.fromTo(Version.V1_14, Version.V1_15_2) -> write114(byteBuf, version.more(Version.V1_15))
            version.fromTo(Version.V1_16, Version.V1_17_1) -> write116(byteBuf, version.moreOrEqual(Version.V1_16_2))
            version.fromTo(Version.V1_18, Version.V1_18_2) -> write118(byteBuf, version == Version.V1_18_2)
            version.fromTo(Version.V1_19, Version.V1_20) -> write119(byteBuf, version.toNbtVersion())
            version.moreOrEqual(Version.V1_20_2) -> write1202(byteBuf)
        }
    }

    private fun write17(byteBuf: ByteMessage, v18: Boolean, v19: Boolean) {
        byteBuf.writeByte(if (gameMode == 3 && !v18) 1 else gameMode) // 1.7 not supported spectator
        dimension.dimensionId.let { if (v19) byteBuf.writeInt(it) else byteBuf.writeByte(it) }
        byteBuf.writeByte(0) // difficulty
        byteBuf.writeByte(0) // max players
        byteBuf.writeString("flat") // level type
        if (v18) byteBuf.writeBoolean(reducedDebugInfo)
    }

    private fun write114(byteBuf: ByteMessage, v15: Boolean) {
        byteBuf.writeByte(gameMode)
        byteBuf.writeInt(dimension.dimensionId)
        if (v15) byteBuf.writeLong(hashedSeed)
        byteBuf.writeByte(0) // max players
        byteBuf.writeString("flat")
        byteBuf.writeVarInt(viewDistance)
        byteBuf.writeBoolean(reducedDebugInfo)
        if (v15) byteBuf.writeBoolean(showRespawnScreen)
    }

    private fun write116(byteBuf: ByteMessage, v1162: Boolean) {
        if (v1162) byteBuf.writeBoolean(isHardcore)
        byteBuf.writeByte(gameMode)
        byteBuf.writeByte(-1)
        byteBuf.writeStringsArray(levelNames)
        val nbtVersion = if (v1162) NbtVersion.V1_16_2 else NbtVersion.LEGACY
        byteBuf.writeCompoundTag(dimension.toTag(nbtVersion) as CompoundBinaryTag)
        if (v1162)
            byteBuf.writeCompoundTag(dimension.getAttributes(nbtVersion))
        else
            byteBuf.writeString(dimension.key)
        byteBuf.writeString(levelName)
        byteBuf.writeLong(hashedSeed)
        if (v1162) byteBuf.writeVarInt(0) else byteBuf.writeByte(0) // max players
        byteBuf.writeVarInt(viewDistance)
        byteBuf.writeBoolean(reducedDebugInfo)
        byteBuf.writeBoolean(showRespawnScreen)
        byteBuf.writeBoolean(false) // debug type
        byteBuf.writeBoolean(false) // flat
    }

    private fun write118(byteBuf: ByteMessage, v1182: Boolean) {
        byteBuf.writeBoolean(isHardcore)
        byteBuf.writeByte(gameMode)
        byteBuf.writeByte(-1)
        byteBuf.writeStringsArray(levelNames)
        val nbtVersion = if (v1182) NbtVersion.V1_18_2 else NbtVersion.V1_16_2
        byteBuf.writeCompoundTag(dimension.toTag(nbtVersion) as CompoundBinaryTag)
        byteBuf.writeCompoundTag(dimension.getAttributes(nbtVersion))
        byteBuf.writeString(levelName)
        byteBuf.writeLong(hashedSeed)
        byteBuf.writeVarInt(0) // max players
        byteBuf.writeVarInt(viewDistance)
        byteBuf.writeVarInt(viewDistance) // simulation distance
        byteBuf.writeBoolean(reducedDebugInfo)
        byteBuf.writeBoolean(showRespawnScreen)
        byteBuf.writeBoolean(false)
        byteBuf.writeBoolean(false)
    }

    private fun write119(byteBuf: ByteMessage, nbtVersion: NbtVersion) {
        byteBuf.writeBoolean(isHardcore)
        byteBuf.writeByte(gameMode)
        byteBuf.writeByte(-1)
        byteBuf.writeStringsArray(levelNames)
        byteBuf.writeCompoundTag(dimension.toTag(nbtVersion) as CompoundBinaryTag)
        byteBuf.writeString(levelName) // world type
        byteBuf.writeString(levelName)
        byteBuf.writeLong(hashedSeed)
        byteBuf.writeVarInt(0)
        byteBuf.writeVarInt(viewDistance)
        byteBuf.writeVarInt(viewDistance)
        byteBuf.writeBoolean(reducedDebugInfo)
        byteBuf.writeBoolean(showRespawnScreen)
        byteBuf.writeBoolean(false)
        byteBuf.writeBoolean(false)
        byteBuf.writeBoolean(false) // no last death location
        if (nbtVersion == NbtVersion.V1_20) byteBuf.writeVarInt(0)
    }

    private fun write1202(byteBuf: ByteMessage) {
        byteBuf.writeBoolean(isHardcore)
        byteBuf.writeStringsArray(levelNames)
        byteBuf.writeVarInt(0) // max players
        byteBuf.writeVarInt(viewDistance)
        byteBuf.writeVarInt(viewDistance)
        byteBuf.writeBoolean(reducedDebugInfo)
        byteBuf.writeBoolean(showRespawnScreen)
        byteBuf.writeBoolean(false) // limited crafting
        byteBuf.writeString(levelName)
        byteBuf.writeString(levelName)
        byteBuf.writeLong(hashedSeed)
        byteBuf.writeByte(gameMode)
        byteBuf.writeByte(-1)
        byteBuf.writeBoolean(false)
        byteBuf.writeBoolean(false)
        byteBuf.writeBoolean(false)
        byteBuf.writeVarInt(0)
    }

}