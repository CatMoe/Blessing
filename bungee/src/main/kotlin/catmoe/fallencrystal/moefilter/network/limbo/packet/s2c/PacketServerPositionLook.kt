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

import catmoe.fallencrystal.moefilter.network.common.ByteMessage
import catmoe.fallencrystal.moefilter.network.limbo.packet.LimboS2CPacket
import catmoe.fallencrystal.moefilter.network.limbo.LimboLocation
import catmoe.fallencrystal.translation.utils.version.Version

class PacketServerPositionLook: LimboS2CPacket() {

    // Input
    var teleport: Int? = null

    var sendLoc = LimboLocation(0.0, 256.0, 0.0, 0f, 0f, false)

    override fun encode(packet: ByteMessage, version: Version?) {
        packet.writeDouble(sendLoc.x)
        packet.writeDouble(sendLoc.y + (if (version!!.less(Version.V1_8)) 1.62f else 0f))
        packet.writeDouble(sendLoc.z)
        //listOf(sendLoc.yaw, sendLoc.pitch).forEach { packet.writeFloat(it) }
        packet.writeFloat(sendLoc.yaw)
        packet.writeFloat(sendLoc.pitch)
        if (version.moreOrEqual(Version.V1_8)) packet.writeByte(0x08) else packet.writeBoolean(sendLoc.onGround)
        // 1.9+ 的 ConfirmTeleport实际上是在此处处理的 而不是另一个数据包 :|
        if (version.moreOrEqual(Version.V1_9)) packet.writeVarInt(teleport ?: 7890)
        // 是否骑乘在实体身上
        if (version.fromTo(Version.V1_17, Version.V1_19_3)) packet.writeBoolean(false)
    }

    override fun toString(): String {
        return "PacketServerPositionLook(location=$sendLoc, teleportId=$teleport)"
    }

}