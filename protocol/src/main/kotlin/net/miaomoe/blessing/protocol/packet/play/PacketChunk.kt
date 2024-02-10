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

import net.miaomoe.blessing.nbt.NbtUtil.toNamed
import net.miaomoe.blessing.nbt.NbtUtil.toNbt
import net.miaomoe.blessing.protocol.direction.PacketDirection
import net.miaomoe.blessing.protocol.packet.type.PacketBidirectional
import net.miaomoe.blessing.protocol.util.ByteMessage
import net.miaomoe.blessing.protocol.version.Version
import net.miaomoe.blessing.protocol.version.Version.*

/**
 * A packet to sent empty chunk.
 */
@Suppress("MemberVisibilityCanBePrivate")
class PacketChunk(
    var x: Int = 0,
    var z: Int = 0,
) : PacketBidirectional {

    override val forceDirection = PacketDirection.TO_CLIENT

    override fun encode(byteBuf: ByteMessage, version: Version, direction: PacketDirection) {
        byteBuf.writeInt(x)
        byteBuf.writeInt(z)
        if (version.fromTo(V1_17, V1_17_1))
            byteBuf.writeVarInt(0)
        else if (version.less(V1_17)) {
            byteBuf.writeBoolean(true)
            if (version.fromTo(V1_16, V1_16_1)) byteBuf.writeBoolean(true)
            if (version.more(V1_8)) byteBuf.writeVarInt(0) else byteBuf.writeShort(1)
        }

        if (version.moreOrEqual(V1_14)) {
            if (version.moreOrEqual(V1_20_2))
                byteBuf.writeNamelessTag(modernTag)
            else
                byteBuf.writeCompoundTag(if (version.less(V1_18)) legacyTag else modernTag)
            if (version.fromTo(V1_15, V1_17_1)) {
                val range = 0 until  1024
                if (version.moreOrEqual(V1_16_2)) {
                    byteBuf.writeVarInt(range.last)
                    for (i in range) byteBuf.writeVarInt(1)
                } else
                    for (i in range) byteBuf.writeInt(0)
            }
        }

        when {
            version.less(V1_8) -> {
                byteBuf.writeInt(0)
                byteBuf.writeBytes(filler1)
            }
            version.fromTo(V1_8, V1_12_2) -> byteBuf.writeBytesArray(filler2)
            version.fromTo(V1_13, V1_14_4) -> byteBuf.writeBytesArray(filler3)
            version.fromTo(V1_15, V1_17_1) -> byteBuf.writeVarInt(0)
            else -> {
                byteBuf.writeVarInt(sectionBytes.size * 16)
                for (i in 0 until 16) byteBuf.writeBytes(sectionBytes)
            }
        }

        if (version.moreOrEqual(V1_9_4)) byteBuf.writeVarInt(0)

        if (version.moreOrEqual(V1_18)) {
            byteBuf.ensureWritable(lightBytes.size)
            if (version.moreOrEqual(V1_20))
                byteBuf.writeBytes(lightBytes, 1, lightBytes.size - 1)
            else
                byteBuf.writeBytes(lightBytes)
        }
    }

    override fun decode(byteBuf: ByteMessage, version: Version, direction: PacketDirection) {
        this.x = byteBuf.readInt()
        this.z = byteBuf.readInt()
    }

    override fun toString() = "PacketChunk(x=$x, z=$z)"

    companion object {
        private val sectionBytes = byteArrayOf(0, 0, 0, 0, 0, 0, 1, 0)
        private val lightBytes = byteArrayOf(1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 3, -1, -1, 0, 0)
        private val filler1 = ByteArray(2)
        private val filler2 = ByteArray(256)
        private val filler3 = ByteArray(256 * 4)
        private val legacyTag = getEmptyMotionBlock(36)
        private val modernTag = getEmptyMotionBlock(37)

        private fun getEmptyMotionBlock(size: Int) = ByteArray(size).toNbt().toNamed("MOTION_BLOCKING").toNamed("root")
    }

}