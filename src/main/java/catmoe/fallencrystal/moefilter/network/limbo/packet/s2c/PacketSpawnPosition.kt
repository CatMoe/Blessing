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

import catmoe.fallencrystal.moefilter.network.limbo.packet.ByteMessage
import catmoe.fallencrystal.moefilter.network.limbo.packet.LimboS2CPacket
import catmoe.fallencrystal.moefilter.network.limbo.util.LimboLocation
import catmoe.fallencrystal.moefilter.network.limbo.util.Version

@Suppress("MemberVisibilityCanBePrivate")
class PacketSpawnPosition : LimboS2CPacket() {

    var location = LimboLocation(0.0, 450.0, 0.0, 0f, 0f, false)
    var angle = 0f

    override fun encode(packet: ByteMessage, version: Version?) {
        packet.writeLong(
            if (version!!.less(Version.V1_14))
                (location.x.toInt() and 0x3FFFFFF shl 38 or (location.y.toInt() and 0xFFF shl 26) or (location.z.toInt() and 0x3FFFFFF)).toLong()
            else
                (location.x.toInt() and 0x3FFFFFF shl 38 or (location.z.toInt() and 0x3FFFFFF shl 12) or (location.y.toInt() and 0xFFF)).toLong()
        )
        if (version.moreOrEqual(Version.V1_17)) { packet.writeFloat(angle) /* Actually, that angle for 1.17+. But now we don't need that. */ }
    }

    override fun toString(): String {
        return "location=$location"
    }

}