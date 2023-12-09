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
import catmoe.fallencrystal.moefilter.network.limbo.packet.LimboS2CPacket
import catmoe.fallencrystal.translation.utils.version.Version

@Deprecated("Use PacketBlocksSectionUpdate")
@Suppress("MemberVisibilityCanBePrivate")
class PacketBlockUpdate(
    var block: BlockPosition? = null
) : LimboS2CPacket() {
    override fun encode(byteBuf: ByteMessage, version: Version?) {
        val block = this.block!!
        if (version!!.moreOrEqual(Version.V1_8)) {
            byteBuf.writeLong(
                (if (version.more(Version.V1_13_2))
                    ((block.x and 0x3FFFFFF shl 38) or (block.z and 0x3FFFFFF shl 12) or (block.y and 0xFFF))
                else
                    (block.x and 0x3FFFFFF shl 38 or (block.y and 0xFFF shl 26) or (block.z and 0x3FFFFFF))).toLong()
            )
            byteBuf.writeVarInt(block.block.getId(version))
        } else {
            byteBuf.writeInt(block.x)
            byteBuf.writeByte(block.y)
            byteBuf.writeInt(block.z)
            byteBuf.writeVarInt(block.block.getId(version))
            byteBuf.writeByte(0)
        }
    }

}