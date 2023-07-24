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

package catmoe.fallencrystal.moefilter.network.bungee.limbo.packet.s2c

import catmoe.fallencrystal.moefilter.network.bungee.limbo.handshake.Version
import catmoe.fallencrystal.moefilter.network.bungee.limbo.packet.ByteMessage
import catmoe.fallencrystal.moefilter.network.bungee.limbo.packet.LimboS2CPacket
import io.netty.channel.Channel

@Suppress("unused", "MemberVisibilityCanBePrivate")
class PacketPositionAndLook: LimboS2CPacket() {

    var x: Double = 0.0
    var y: Double = 256.0
    var z: Double = 0.0
    var yaw: Float = 0f
    var pitch: Float = 0f
    var teleport = 7890

    override fun encode(packet: ByteMessage, channel: Channel, version: Version?) {
        // 关于1.7的y的1.62Float轴偏移的话 还是请Mojang自己出来发言吧(x
        listOf(x, this.y + (if (version!!.less(Version.V1_8)) 1.62f else 0f), z).forEach { packet.writeDouble(it) }
        listOf(yaw, pitch).forEach { packet.writeFloat(it) }
        // 处理传送. 如果没有这个 有一个很经典的例子就是.. 套了两层Via然后拿1.8进去发现自己在虚空..
        // 至于1.7嘛.. 那玩意着实很奇怪 我也没打算完全做兼容
        if (version.moreOrEqual(Version.V1_8)) packet.writeByte(0x08) else packet.writeBoolean(true)
        // 1.9+ 的 ConfirmTeleport实际上是在此处处理的 而不是另一个数据包 :|
        if (version.moreOrEqual(Version.V1_9)) packet.writeVarInt(teleport)
        // 1.17-1.19.3 (在763(1.20)中被再次移除?有待证实) 中需要通过写入Boolean来告诉客户端它们是否坐在实体上.
        if (version.fromTo(Version.V1_17, Version.V1_19_3)) packet.writeBoolean(false)
    }
}