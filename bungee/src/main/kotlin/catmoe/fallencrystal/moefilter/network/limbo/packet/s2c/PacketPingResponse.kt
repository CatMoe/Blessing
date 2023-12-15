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

@Suppress("MemberVisibilityCanBePrivate")
class PacketPingResponse(
    var protocol: Version = Version.UNDEFINED,
    var brand: String = "MoeLimbo",
    var description: String = "{\"text\": \"§dMoeLimbo\"}",
    var max: Int = 1,
    var online: Int = 0,
    // Protocol (ServerBrand, Protocol), PlayerInfo (1, 0, List<UUID,String>), Description
    var output: String? = null
) : LimboS2CPacket() {

    private val template = "{ \"version\": { \"name\": \"[brand]\", \"protocol\": [protocol] }, " +
            "\"players\": { \"max\": [max], \"online\": [online], \"sample\": [] }, " +
            "\"description\": [description] }"

    override fun encode(byteBuf: ByteMessage, version: Version?) {
        val protocol = if (this.protocol == Version.UNDEFINED) version!! else this.protocol
        val output = if (output != null && output != "") output else {
            this.template
                .replace("[brand]", brand)
                .replace("[protocol]", protocol.number.toString())
                .replace("[max]", max.toString())
                .replace("[online]", online.toString())
                .replace("[description]", description)
        }
        byteBuf.writeString(output)
    }

    override fun toString(): String {
        return "PacketPingResponse($output)"
    }


}