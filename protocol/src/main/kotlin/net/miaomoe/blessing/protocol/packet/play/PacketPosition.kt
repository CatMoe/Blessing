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
import net.miaomoe.blessing.protocol.version.Version

@Suppress("MemberVisibilityCanBePrivate")
class PacketPosition(
    var position: Position = Position.zero,
    var onGround: Boolean = false
) : PacketBidirectional {

    override val forceDirection = PacketDirection.TO_SERVER

    override fun decode(byteBuf: ByteMessage, version: Version, direction: PacketDirection) {
        val x = byteBuf.readDouble()
        if (version.lessOrEqual(Version.V1_7_6)) byteBuf.readDouble() // head y-axis
        position = Position(x, byteBuf.readDouble(), byteBuf.readDouble())
        onGround = byteBuf.readBoolean()
    }

    override fun encode(byteBuf: ByteMessage, version: Version, direction: PacketDirection) {
        byteBuf.writeDouble(position.x)
        if (version.less(Version.V1_8)) byteBuf.writeDouble(position.y + 2)
        byteBuf.writeDouble(position.y)
        byteBuf.writeBoolean(onGround)
    }

    override fun toString() = "PacketPosition(position=$position, onGround=$onGround)"

}