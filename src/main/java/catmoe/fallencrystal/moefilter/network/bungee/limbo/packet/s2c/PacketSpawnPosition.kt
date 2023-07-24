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

@Suppress("MemberVisibilityCanBePrivate")
class PacketSpawnPosition : LimboS2CPacket() {

    var x = 0
    var y = 255
    var z = 0

    override fun encode(packet: ByteMessage, channel: Channel, version: Version?) {
        packet.writeLong((x and 0x3FFFFFF shl 38 or (z and 0x3FFFFFF shl 12) or (y and 0xFFF)).toLong())
        packet.writeFloat(0f) // Actually that is yaw + pitch. but now we don't need that.
    }

}