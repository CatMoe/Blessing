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

import net.miaomoe.blessing.protocol.direction.PacketDirection
import net.miaomoe.blessing.protocol.packet.type.PacketBidirectional
import net.miaomoe.blessing.protocol.util.ByteMessage
import net.miaomoe.blessing.protocol.util.Position
import net.miaomoe.blessing.protocol.util.PositionUtil
import net.miaomoe.blessing.protocol.version.Version

@Suppress("MemberVisibilityCanBePrivate")
class PacketSpawnPosition(
    var position: Position = Position.zero
) : PacketBidirectional {

    override val forceDirection = PacketDirection.TO_CLIENT

    override fun encode(byteBuf: ByteMessage, version: Version, direction: PacketDirection) {
        if (version.moreOrEqual(Version.V1_8)) {
            val value = if (version.lessOrEqual(Version.V1_14)) PositionUtil.getLegacySpawnPosition(position) else PositionUtil.getModernSpawnPosition(position)
            byteBuf.writeLong(value)
            if (version.moreOrEqual(Version.V1_17)) byteBuf.writeFloat(0f)
        } else { // 1.7
            position.let {
                byteBuf.writeInt(it.x.toInt())
                byteBuf.writeInt(it.y.toInt())
                byteBuf.writeInt(it.z.toInt())
            }
        }
    }

    override fun decode(byteBuf: ByteMessage, version: Version, direction: PacketDirection) {
        this.position = if (version.less(Version.V1_8))
            Position(
                byteBuf.readInt().toDouble(),
                byteBuf.readInt().toDouble(),
                byteBuf.readInt().toDouble()
            )
        else {
            val value = byteBuf.readLong()
            Position(
                (value shr 38 and 0x3FFFFFF).toDouble(),
                (value shr 26 and 0xFFF).toDouble(),
                (value and 0x3FFFFFF).toDouble()
            )
        }
    }

    override fun toString() = "PacketSpawnPosition(position=$position)"

}