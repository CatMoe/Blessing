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

package catmoe.fallencrystal.moefilter.network.limbo.captcha

import catmoe.fallencrystal.moefilter.network.limbo.netty.ByteMessage
import catmoe.fallencrystal.moefilter.network.limbo.packet.LimboS2CPacket
import catmoe.fallencrystal.moefilter.network.limbo.util.Version

@Suppress("MemberVisibilityCanBePrivate")
class PacketMapData : LimboS2CPacket() {

    var mapId: Int? = null
    var scale: Byte? = null
    var columns: Int? = null
    var rows: Int? = null
    var x: Int? = null
    var y: Int? = null
    var data: ByteArray? = null

    override fun encode(packet: ByteMessage, version: Version?) {
        packet.writeVarInt(mapId!!)
        packet.writeByte(scale!!.toInt())
        if (version!!.fromTo(Version.V1_9, Version.V1_16_4)) { packet.writeBoolean(false) }
        if (version.moreOrEqual(Version.V1_14)) packet.writeBoolean(false)
        if (version.moreOrEqual(Version.V1_17)) packet.writeBoolean(false) else packet.writeVarInt(0)
        packet.writeByte(columns!!)
        packet.writeByte(rows!!)
        packet.writeByte(x!!)
        packet.writeByte(y!!)
        packet.ensureWritable(3 + data!!.size)
        packet.writeBytesArray(data!!)
    }
}