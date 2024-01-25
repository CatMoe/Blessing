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

import net.miaomoe.blessing.protocol.packet.type.PacketBidirectional
import net.miaomoe.blessing.protocol.util.ByteMessage
import net.miaomoe.blessing.protocol.util.Position
import net.miaomoe.blessing.protocol.version.Version

@Suppress("MemberVisibilityCanBePrivate")
class PacketPositionLook(
    var position: Position = Position.zero,
    var yaw: Float = 0f,
    var pitch: Float = 0f,
    var onGround: Boolean = false,
    var teleportId: Int = 0
) : PacketBidirectional {

    override fun decode(byteBuf: ByteMessage, version: Version) {
        val x = byteBuf.readDouble()
        if (version.lessOrEqual(Version.V1_7_6)) byteBuf.readDouble() // Head y-axis
        position = Position(x, byteBuf.readDouble(), byteBuf.readDouble())
        yaw = byteBuf.readFloat()
        pitch = byteBuf.readFloat()
        onGround = byteBuf.readBoolean()
    }

    override fun encode(byteBuf: ByteMessage, version: Version) {
        byteBuf.writeDouble(position.x)
        byteBuf.writeDouble(position.y)
        byteBuf.writeDouble(position.z)
        byteBuf.writeFloat(yaw)
        byteBuf.writeFloat(pitch)
        if (version.lessOrEqual(Version.V1_8))
            byteBuf.writeBoolean(onGround)
        else {
            byteBuf.writeByte(0x00)
            if (version.moreOrEqual(Version.V1_9)) byteBuf.writeVarInt(teleportId)
            if (version.fromTo(Version.V1_17, Version.V1_19_3)) byteBuf.writeBoolean(true)
        }
    }

}