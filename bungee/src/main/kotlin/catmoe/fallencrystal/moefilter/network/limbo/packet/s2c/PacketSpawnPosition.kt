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
import catmoe.fallencrystal.moefilter.network.limbo.LimboLocation
import catmoe.fallencrystal.moefilter.network.limbo.packet.LimboS2CPacket
import catmoe.fallencrystal.translation.utils.version.Version


@Suppress("MemberVisibilityCanBePrivate")
class PacketSpawnPosition(
    var location: LimboLocation = LimboLocation(0.0, 450.0, 0.0, 0f, 0f, false),
    var angle: Float = 0f
) : LimboS2CPacket() {

    override fun encode(byteBuf: ByteMessage, version: Version?) {

        /*
        packet.writeLong(
            if (version!!.less(Version.V1_14))
                (location.x.toInt() and 0x3FFFFFF shl 38 or (location.y.toInt() and 0xFFF shl 26) or (location.z.toInt() and 0x3FFFFFF)).toLong()
            else
                (location.x.toInt() and 0x3FFFFFF shl 38 or (location.z.toInt() and 0x3FFFFFF shl 12) or (location.y.toInt() and 0xFFF)).toLong()
        )
         */
        byteBuf.writeLong(
            (location.x.toInt() and 0x3FFFFFF shl 38
                    or (if (version!!.less(Version.V1_14)) location.y.toInt() and 0xFFF shl 26 else location.z.toInt() and 0x3FFFFFF shl 12)
                    or (if (version.less(Version.V1_14)) location.z.toInt() and 0x3FFFFFF else location.y.toInt() and 0xFFF )
                    ).toLong()
        )
        if (version.moreOrEqual(Version.V1_17) || version == Version.V1_7_6) { byteBuf.writeFloat(angle) /* Actually, that angle for 1.17+. But now we don't need that. */ }

        /*
        packet.writeLong((location.x.toInt() and 0x3FFFFFF shl 38 or (location.z.toInt() and 0x3FFFFFF shl 12) or (location.y.toInt() and 0xFFF)).toLong())
        packet.writeFloat(angle)
         */
    }

    override fun toString(): String { return "PacketSpawnPosition(location=$location)" }

}