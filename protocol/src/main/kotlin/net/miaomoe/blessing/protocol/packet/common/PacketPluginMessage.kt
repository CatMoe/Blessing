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

package net.miaomoe.blessing.protocol.packet.common

import net.miaomoe.blessing.protocol.direction.PacketDirection
import net.miaomoe.blessing.protocol.packet.type.PacketBidirectional
import net.miaomoe.blessing.protocol.util.ByteMessage
import net.miaomoe.blessing.protocol.version.Version

@Suppress("MemberVisibilityCanBePrivate")
class PacketPluginMessage(
    var channel: String = "",
    var data: ByteArray = byteArrayOf()
) : PacketBidirectional {

    override fun encode(byteBuf: ByteMessage, version: Version, direction: PacketDirection) {
        byteBuf.writeString(channel)
        if (version.moreOrEqual(Version.V1_8))
            byteBuf.writeBytes(data)
        else {
            val data = this.data
            require(data.size < Short.MAX_VALUE) { "Plugin message data is larger than 1.7 clients can accept!" }
            byteBuf.writeShort(data.size)
            byteBuf.writeBytes(data)
        }
    }

    override fun decode(byteBuf: ByteMessage, version: Version, direction: PacketDirection) {
        channel = byteBuf.readString()
        require(channel.isNotBlank() && channel.length in 1..128) { "Invalid channel: $channel" }
        if (version.moreOrEqual(Version.V1_8)) {
            val data = ByteArray(byteBuf.readableBytes())
            byteBuf.readBytes(data)
            this.data=data
        } else {
            this.data = byteBuf.readBytesArray(byteBuf.readShort().toInt())
        }
    }

    override fun toString() = "PacketPluginMessage(channel=$channel, data=${data.contentToString()})"


}