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

package net.miaomoe.blessing.protocol.packet.login

import net.miaomoe.blessing.protocol.direction.PacketDirection
import net.miaomoe.blessing.protocol.packet.type.PacketBidirectional
import net.miaomoe.blessing.protocol.util.ByteMessage
import net.miaomoe.blessing.protocol.version.Version

@Suppress("MemberVisibilityCanBePrivate")
class PacketLoginRequest(var name: String = "") : PacketBidirectional {

    override val forceDirection = PacketDirection.TO_SERVER

    override fun decode(byteBuf: ByteMessage, version: Version, direction: PacketDirection) {
        this.name = byteBuf.readString(limit = 16)
        require(name.length in 3..16) { "Invalid name length!" }
    }

    override fun encode(byteBuf: ByteMessage, version: Version, direction: PacketDirection) {
        byteBuf.writeString(name)
    }

    override fun toString() = "${this::class.simpleName}(name=$name)"

}