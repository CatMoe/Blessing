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

package catmoe.fallencrystal.moefilter.network.limbo.packet.s2c

import catmoe.fallencrystal.moefilter.network.common.ByteMessage
import catmoe.fallencrystal.moefilter.network.limbo.packet.LimboS2CPacket
import catmoe.fallencrystal.translation.utils.version.Version
import se.llbit.nbt.CompoundTag
import se.llbit.nbt.LongArrayTag
import se.llbit.nbt.NamedTag


class PacketEmptyChunk(
    var x: Int = 0,
    var z: Int = 0
) : LimboS2CPacket() {

    override fun encode(byteBuf: ByteMessage, version: Version?) {
        // Chunk pos
        byteBuf.writeInt(x)
        byteBuf.writeInt(z)
        if (version!!.less(Version.V1_17)) {
            byteBuf.writeBoolean(true)
            if (version.fromTo(Version.V1_16, Version.V1_16_1)) byteBuf.writeBoolean(true)
            if (version.moreOrEqual(Version.V1_9)) byteBuf.writeVarInt(0) else byteBuf.writeShort(1)
        } else if (version.fromTo(Version.V1_17, Version.V1_17_1)) byteBuf.writeVarInt(0)
        if (version.moreOrEqual(Version.V1_14)) {
            val tag = CompoundTag()
            tag.add("MOTION_BLOCKING", LongArrayTag(LongArray(if (version.less(Version.V1_18)) 36 else 37)))
            val rootTag = CompoundTag()
            rootTag.add("root", tag)
            if (version.moreOrEqual(Version.V1_20_2))
                byteBuf.writeNamelessCompoundTag(rootTag)
            else
                byteBuf.writeCompoundTag(NamedTag("", rootTag))

            if (version.fromTo(Version.V1_15, Version.V1_17_1)) {
                val intRange = 0 until 1024
                if (version.moreOrEqual(Version.V1_16_2)) {
                    byteBuf.writeVarInt(1024)
                    for (i in intRange) byteBuf.writeVarInt(1)
                } else {  for (i in intRange) byteBuf.writeInt(0) }
            }
        }
        when {
            version.fromTo(Version.V1_7_2, Version.V1_7_6) -> {
                byteBuf.writeInt(0)
                byteBuf.writeBytes(ByteArray(2))
            }
            version.fromTo(Version.V1_8, Version.V1_12_2) -> byteBuf.writeBytesArray(ByteArray(256))
            version.fromTo(Version.V1_13, Version.V1_14_4) -> byteBuf.writeBytesArray(ByteArray(1024))
            version.fromTo(Version.V1_15, Version.V1_17_1) -> byteBuf.writeVarInt(0)
            else -> {
                val sectionData = byteArrayOf(0, 0, 0, 0, 0, 0, 1, 0)
                byteBuf.writeVarInt(sectionData.size * 16)
                for (i in 0 until 16) byteBuf.writeBytes(sectionData)
            }
        }
        if (version.moreOrEqual(Version.V1_9_4)) byteBuf.writeVarInt(0)
        if (version.moreOrEqual(Version.V1_18)) { // Light data
            val lightData = byteArrayOf(1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 3, -1, -1, 0, 0)
            byteBuf.ensureWritable(lightData.size)
            if (version.moreOrEqual(Version.V1_20))
                byteBuf.writeBytes(lightData, 1, lightData.size - 1)
            else
                byteBuf.writeBytes(lightData)
        }
    }


    override fun toString() = "PacketEmptyChunk(TargetX=$x, TargetZ=$z)"
}