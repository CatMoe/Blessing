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
import catmoe.fallencrystal.moefilter.network.limbo.packet.LimboS2CPacket
import catmoe.fallencrystal.translation.utils.version.Version
import java.util.*

class PacketLoginSuccess : LimboS2CPacket() {
    var uuid: UUID? = null
    var username = ""
    override fun encode(packet: ByteMessage, version: Version?) {
        val uuid = this.uuid ?: UUID.randomUUID()
        if (version!!.moreOrEqual(Version.V1_16)) packet.writeUuid(uuid)
        else packet.writeString(uuid.toString())
        packet.writeString(username)
        if (version.moreOrEqual(Version.V1_19)) packet.writeVarInt(0)
    }

    override fun toString(): String { return "PacketLoginSuccess(uuid=$uuid, username=$username)" }
}