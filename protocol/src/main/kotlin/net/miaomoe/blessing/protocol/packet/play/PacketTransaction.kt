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
import net.miaomoe.blessing.protocol.version.Version

@Suppress("MemberVisibilityCanBePrivate")
class PacketTransaction(
    var windowId: Int = 0,
    var id: Int = 0,
    var accepted: Boolean = true
) : PacketBidirectional {

    override fun encode(byteBuf: ByteMessage, version: Version) {
        if (version.moreOrEqual(Version.V1_17))
            byteBuf.writeInt(id)
        else {
            byteBuf.writeByte(windowId)
            byteBuf.writeShort(id)
            byteBuf.writeBoolean(accepted)
        }
    }

    override fun decode(byteBuf: ByteMessage, version: Version) {
        if (version.moreOrEqual(Version.V1_17)) {
            id = byteBuf.readInt()
            accepted=true
        } else {
            windowId = byteBuf.readByte().toInt()
            id = byteBuf.readShort().toInt()
            accepted = byteBuf.readBoolean()
        }
    }

}