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
import catmoe.fallencrystal.moefilter.network.limbo.handler.MoeLimbo
import catmoe.fallencrystal.moefilter.network.limbo.packet.LimboS2CPacket
import catmoe.fallencrystal.translation.utils.version.Version

// Refs: https://github.com/jonesdevelopment/sonar/blob/main/sonar-common/src/main/java/xyz/jonesdev/sonar/common/fallback/protocol/packets/play/UpdateSectionBlocks.java
@Suppress("MemberVisibilityCanBePrivate")
class PacketBlocksSectionUpdate(
    val blocks: MutableCollection<BlockPosition> = mutableListOf(),
    var sectionX: Int = 0,
    var sectionZ: Int = 0
) : LimboS2CPacket() {

    override fun encode(packet: ByteMessage, version: Version?) {
        if (version!!.less(Version.V1_16_2)) {
            packet.writeInt(sectionX)
            packet.writeInt(sectionZ)

            if (version.less(Version.V1_8)) {
                packet.writeShort(blocks.size)
                packet.writeInt(4 * blocks.size)
            } else packet.writeVarInt(blocks.size)

            for (block in blocks) {
                packet.writeShort((block.x - (sectionX shl 4)) shl 12 or ((block.z - (sectionZ shl 4)) shl 8) or block.y)
                if (version.moreOrEqual(Version.V1_13)) packet.writeVarInt(block.block.getId(version)) else {
                    val id = block.block.getId(version) shl 4
                    if (version.moreOrEqual(Version.V1_8)) packet.writeVarInt(id) else packet.writeShort(id)
                }
            }
        } else {
            val chunkY = (blocks.firstOrNull()?.y ?: 1) shr 4
            var chunk = (0 or (sectionX and 0x3FFFFF shl 42)).toLong()
            packet.writeLong((sectionZ.toLong() and 0x3FFFFFL shl 20).let { chunk = chunk or it; chunk } or (chunkY.toLong() and 0xFFFFFL))
            if (version.less(Version.V1_20)) packet.writeBoolean(true) // Suppress light updates. But removed on 1.20
            packet.writeVarInt(blocks.size)
            for (block in blocks) {
                val id = block.block.getId(version)
                val position = (block.x - (sectionX shl 4) shl 8 or (block.z - (sectionZ shl 4) shl 4) or block.y - (chunkY shl 4)).toShort()
                val value = id.toLong() shl 12 or position.toLong()
                MoeLimbo.debug(" SectionUpdate (Block: ${block.x}, ${block.y}, ${block.z}, ${id}): Position=$position, value=$value")
                packet.writeVarLong(value)
            }
        }
    }
}