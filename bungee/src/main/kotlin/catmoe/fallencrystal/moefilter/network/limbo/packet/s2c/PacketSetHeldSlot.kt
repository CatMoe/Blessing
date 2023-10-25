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
class PacketSetHeldSlot : LimboS2CPacket() {

    var windowId = 1234
    var slot = 36
    var item = 0
    var count = 0
    var data = 0

    override fun encode(packet: ByteMessage, version: Version?) {
        packet.writeByte(windowId)

        if (version!!.moreOrEqual(Version.V1_17_1)) {
            packet.writeVarInt(0)
        }

        packet.writeShort(slot)
        val id = if (item == 358) getCaptchaId(version) else item
        val present = id > 0

        if (version.moreOrEqual(Version.V1_13_2)) {
            packet.writeBoolean(present)
        }

        if (!present && version.less(Version.V1_13_2)) {
            packet.writeShort(-1)
        }

        if (present) {
            if (version.less(Version.V1_13_2)) { packet.writeShort(id) } else { packet.writeVarInt(id) }
            packet.writeByte(count)
            if (version.less(Version.V1_13)) { packet.writeShort(data) }
            if (version.less(Version.V1_17)) { packet.writeByte(0) //No Nbt
            } else {
                // TODO
            }
        }
    }
    private fun getCaptchaId(version: Version): Int {
        return when {
            version.lessOrEqual(Version.V1_12) -> 358
            version.lessOrEqual(Version.V1_13) -> 608
            version.lessOrEqual(Version.V1_13_2) -> 613
            version.lessOrEqual(Version.V1_15_2) -> 671
            version.lessOrEqual(Version.V1_16_4) -> 733
            version.lessOrEqual(Version.V1_18_2) -> 847
            version.lessOrEqual(Version.V1_19_1) -> 886
            version.lessOrEqual(Version.V1_19_3) -> 914
            version.lessOrEqual(Version.V1_19_4) -> 937
            else -> 941
        }
    }

    override fun toString(): String {
        return "PacketSetHeldSlot(windowId=$windowId, slot=$slot, item=$item, count=$count, data=$data)"
    }
}