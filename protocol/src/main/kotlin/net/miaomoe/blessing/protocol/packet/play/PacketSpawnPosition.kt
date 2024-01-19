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

import net.miaomoe.blessing.protocol.packet.type.PacketToClient
import net.miaomoe.blessing.protocol.util.ByteMessage
import net.miaomoe.blessing.protocol.util.Position
import net.miaomoe.blessing.protocol.util.PositionUtil
import net.miaomoe.blessing.protocol.version.Version

@Suppress("MemberVisibilityCanBePrivate")
class PacketSpawnPosition(
    var position: Position = Position(0, 0, 0)
) : PacketToClient {

    override fun encode(byteBuf: ByteMessage, version: Version) {
        if (version.moreOrEqual(Version.V1_8)) {
            val value = if (version.moreOrEqual(Version.V1_14))
                PositionUtil.getModernSpawnPosition(position)
            else
                PositionUtil.getLegacySpawnPosition(position)
            byteBuf.writeLong(value.toLong())
            if (version.moreOrEqual(Version.V1_17)) byteBuf.writeFloat(0f)
        } else { // 1.7
            position.let {
                byteBuf.writeInt(it.x)
                byteBuf.writeInt(it.y)
                byteBuf.writeInt(it.z)
            }
        }
    }

    override fun toString() = "PacketSpawnPosition(position=$position)"

}