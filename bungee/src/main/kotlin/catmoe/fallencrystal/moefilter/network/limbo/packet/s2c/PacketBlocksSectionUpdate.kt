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
import catmoe.fallencrystal.moefilter.network.limbo.block.BlockPosition
import catmoe.fallencrystal.moefilter.network.limbo.handler.LimboLoader
import catmoe.fallencrystal.moefilter.network.limbo.packet.LimboS2CPacket
import catmoe.fallencrystal.translation.utils.version.Version

@Suppress("MemberVisibilityCanBePrivate")
class PacketBlocksSectionUpdate(
    val blocks: MutableCollection<BlockPosition> = mutableListOf(),
    var sectionX: Int = 0,
    var sectionZ: Int = 0
) : LimboS2CPacket() {

    override fun encode(byteBuf: ByteMessage, version: Version?) {
        if (version!!.less(Version.V1_16_2)) {
            byteBuf.writeInt(sectionX)
            byteBuf.writeInt(sectionZ)
            byteBuf.writeVarInt(blocks.size)
            for (block in blocks) {
                val position = (block.x - (sectionX shl 4)) shl 12 or ((block.z - (sectionZ shl 4)) shl 8) or block.y
                val id = block.block.getId(version)
                val writeId = if (version.moreOrEqual(Version.V1_13)) id else id shl 4
                LimboLoader.debug(" SectionUpdate (Block: ${block.x}, ${block.y}, ${block.z}, ${id}): Position=$position, writeId=$writeId")
                byteBuf.writeShort(position)
                byteBuf.writeVarInt(writeId)
            }
        } else {
            val sectionX: Long = (this.sectionX shr 42).toLong()
            val sectionY: Long = (blocks.first().y shl 44 shr 44).toLong()
            val sectionZ: Long = (this.sectionZ shl 22 shr 42).toLong()
            val section = sectionX and 0x3FFFFF shl 42 or (sectionY and 0xFFFFF) or (sectionZ and 0x3FFFFF shl 20)
            LimboLoader.debug(" SectionUpdate: ")
            LimboLoader.debug("   x=${this.sectionX}, z=${this.sectionZ}")
            LimboLoader.debug("   shiftedX=$sectionX, Y=$sectionY, shiftedZ=$sectionZ")
            byteBuf.writeLong(section)
            val size = blocks.size
            byteBuf.writeVarInt(size)
            LimboLoader.debug("   blocks: $size")
            for (block in blocks) {
                val id = block.block.getId(version)
                val blockPosition = id shl 12 or block.x shl 8 or block.z shl 4 or block.y
                LimboLoader.debug("   Block: Non-shifted=$id, Position=$blockPosition")
                byteBuf.writeVarLong(blockPosition.toLong())
            }
        }
    }
}