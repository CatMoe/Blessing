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

class PacketServerPositionLook: LimboS2CPacket() {

    // Input
    var teleport: Int? = null

    var sendLoc = LimboLocation(0.0, 256.0, 0.0, 0f, 0f, false)

    override fun encode(packet: ByteMessage, version: Version?) {
        // 关于1.7的y的1.62Float轴偏移的话 还是请Mojang自己出来发言吧(x
        listOf(
            sendLoc.x,
            sendLoc.y + (if (version!!.less(Version.V1_8)) 1.62f else 0f),
            sendLoc.z
        ).forEach { packet.writeDouble(it) }
        listOf(sendLoc.yaw, sendLoc.pitch).forEach { packet.writeFloat(it) }
        // OnGround
        if (version.moreOrEqual(Version.V1_8)) packet.writeByte(0x08) else packet.writeBoolean(true)
        // 1.9+ 的 ConfirmTeleport实际上是在此处处理的 而不是另一个数据包 :|
        if (version.moreOrEqual(Version.V1_9)) packet.writeVarInt(teleport ?: 7890)
        // 是否起程在实体身上
        if (version.fromTo(Version.V1_17, Version.V1_19_3)) packet.writeBoolean(false)
    }

}