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
import java.util.*


class PacketEmptyChunk(
    var x: Int = 0,
    var z: Int = 0
) : LimboS2CPacket() {

    override fun encode(packet: ByteMessage, version: Version?) {
        // Chunk pos
        packet.writeInt(x)
        packet.writeInt(z)
        if (version!!.less(Version.V1_17)) packet.writeBoolean(true)
        if (version.fromTo(Version.V1_16, Version.V1_16_1)) packet.writeBoolean(true)
        if (version.less(Version.V1_17)) {
            if (version.lessOrEqual(Version.V1_8)) packet.writeShort(1) else packet.writeVarInt(0)
        } else if (version.less(Version.V1_18)) {
            val bitSet = BitSet()
            for (it in 0..15) { bitSet[it] = false }
            val mask = bitSet.toLongArray()
            packet.writeVarInt(mask.size)
            mask.forEach { packet.writeLong(it) }
        }
        if (version.moreOrEqual(Version.V1_14)) {
            // Height maps << Start
            val tag = CompoundTag()
            tag.add("MOTION_BLOCKING", LongArrayTag(LongArray(if (version.less(Version.V1_18)) 36 else 37)))
            val rootTag = CompoundTag()
            rootTag.add("root", tag)
            if (version.moreOrEqual(Version.V1_20_2))
                packet.writeHeadlessCompoundTag(rootTag)
            else
                packet.writeCompoundTag(NamedTag("", rootTag))
            // Height maps >> End
            if (version.fromTo(Version.V1_15_2, Version.V1_17_1)) {
                if (version.moreOrEqual(Version.V1_16_2)) {
                    packet.writeVarInt(1024)
                    (0..1023).forEach { _ -> packet.writeVarInt(1) }
                } else {  (0..1023).forEach { _ -> packet.writeInt(0) } }
            }
        }
        when {
            version.less(Version.V1_13) -> packet.writeBytesArray(ByteArray((256)))
            version.less(Version.V1_15) -> packet.writeBytesArray(ByteArray(1024))
            version.less(Version.V1_18) -> packet.writeVarInt(0)
            else -> {
                val sectionData = byteArrayOf(0, 0, 0, 0, 0, 0, 1, 0)
                packet.writeVarInt(sectionData.size * 16)
                (0..15).forEach { _ -> packet.writeBytes(sectionData) }
            }
        }
        if (version.moreOrEqual(Version.V1_9_4)) packet.writeVarInt(0)
        if (version.moreOrEqual(Version.V1_18)) { // Light data
            val lightData = byteArrayOf(1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 3, -1, -1, 0, 0)
            packet.ensureWritable(lightData.size)
            if (version.moreOrEqual(Version.V1_20))
                packet.writeBytes(lightData, 1, lightData.size - 1)
            else
                packet.writeBytes(lightData)
        }
    }


    override fun toString(): String { return "PacketEmptyChunk(TargetX=$x, TargetZ=$z)" }
}