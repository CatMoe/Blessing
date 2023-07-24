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
import java.util.*

@Suppress("MemberVisibilityCanBePrivate")
class PacketLoginSuccess : LimboS2CPacket() {
    var uuid: UUID? = null
    var username = ""
    override fun encode(packet: ByteMessage, channel: Channel, version: Version?) {
        val uuid = this.uuid ?: UUID.randomUUID()
        if (version!!.moreOrEqual(Version.V1_16)) packet.writeUuid(uuid)
        else if (version.moreOrEqual(Version.V1_7_6)) packet.writeString(uuid.toString())
        else packet.writeString(uuid.toString().replace("-", ""))
        packet.writeString(username)
        if (version.moreOrEqual(Version.V1_19)) packet.writeVarInt(0)
    }
}