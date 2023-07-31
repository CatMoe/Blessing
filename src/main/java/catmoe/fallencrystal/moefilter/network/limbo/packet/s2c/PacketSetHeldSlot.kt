/*
 * Copyright 2023. CatMoe / FallenCrystal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package catmoe.fallencrystal.moefilter.network.limbo.packet.s2c

import catmoe.fallencrystal.moefilter.network.limbo.netty.ByteMessage
import catmoe.fallencrystal.moefilter.network.limbo.packet.LimboS2CPacket
import catmoe.fallencrystal.moefilter.network.limbo.util.Version


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
        return if (version.lessOrEqual(Version.V1_12)) 358 else if (version.lessOrEqual(Version.V1_13)) 608
        else if (version.lessOrEqual(Version.V1_13_2)) 613 else if (version.lessOrEqual(Version.V1_15_2)) 671
        else if (version.lessOrEqual(Version.V1_16_4)) 733 else if (version.lessOrEqual(Version.V1_18_2)) 847
        else if (version.lessOrEqual(Version.V1_19_1)) 886 else if (version.lessOrEqual(Version.V1_19_3)) 914
        else if (version.lessOrEqual(Version.V1_19_4)) 937 else 941
    }

    override fun toString(): String {
        return "windowId=$windowId, slot=$slot, item=$item, count=$count, data=$data"
    }
}