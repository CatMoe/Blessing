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

@file:Suppress("MemberVisibilityCanBePrivate")

package net.miaomoe.blessing.protocol.packet.login

import net.miaomoe.blessing.protocol.direction.PacketDirection
import net.miaomoe.blessing.protocol.packet.type.PacketBidirectional
import net.miaomoe.blessing.protocol.util.ByteMessage
import net.miaomoe.blessing.protocol.util.UUIDUtil
import net.miaomoe.blessing.protocol.version.Version
import java.util.*

class PacketLoginResponse(
    var name: String = "Blessing",
    var uuid: UUID = UUIDUtil.generateOfflinePlayerUuid(name)
) : PacketBidirectional {

    override val forceDirection = PacketDirection.TO_CLIENT

    override fun encode(byteBuf: ByteMessage, version: Version, direction: PacketDirection) {
        when {
            version.moreOrEqual(Version.V1_19) -> byteBuf.writeUUID(uuid)
            version.moreOrEqual(Version.V1_16) -> {
                fun write(value: Long) {
                    byteBuf.writeInt((value ushr 32).toInt())
                    byteBuf.writeInt(value.toInt())
                }
                write(uuid.mostSignificantBits)
                write(uuid.leastSignificantBits)
            }
            version.moreOrEqual(Version.V1_7_6) -> byteBuf.writeString(uuid.toString())
            else -> byteBuf.writeString(UUIDUtil.toLegacyFormat(uuid))
        }
        byteBuf.writeString(name)
        if (version.moreOrEqual(Version.V1_19)) byteBuf.writeVarInt(0)
    }

    override fun decode(byteBuf: ByteMessage, version: Version, direction: PacketDirection) {
        uuid = when {
            version.moreOrEqual(Version.V1_19) -> byteBuf.readUUID()
            version.moreOrEqual(Version.V1_16) -> UUID(0, 0) // Unsupported
            version.moreOrEqual(Version.V1_7_6) -> UUID.fromString(byteBuf.readString())
            else -> UUID(0, 0) // Unsupported
        }
        this.name = byteBuf.readString()
        if (version.moreOrEqual(Version.V1_19)) byteBuf.readVarInt() // ?
    }

    override fun toString() = "${this::class.simpleName}(name=$name, uuid=$uuid)"

}