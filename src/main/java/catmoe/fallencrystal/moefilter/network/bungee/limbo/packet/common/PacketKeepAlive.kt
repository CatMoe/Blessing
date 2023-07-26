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

package catmoe.fallencrystal.moefilter.network.bungee.limbo.packet.common

import catmoe.fallencrystal.moefilter.network.bungee.limbo.packet.ByteMessage
import catmoe.fallencrystal.moefilter.network.bungee.limbo.packet.LimboPacket
import catmoe.fallencrystal.moefilter.network.bungee.limbo.util.Version
import io.netty.channel.Channel

class PacketKeepAlive : LimboPacket {

    var id: Long = 9876

    override fun encode(packet: ByteMessage, version: Version?) {
        if (version!!.moreOrEqual(Version.V1_12_2)) packet.writeLong(id)
        else if (version.moreOrEqual(Version.V1_8)) packet.writeVarInt(id.toInt())
        else packet.writeInt(id.toInt())
    }

    override fun decode(packet: ByteMessage, channel: Channel, version: Version?) {
        id = if (version!!.moreOrEqual(Version.V1_12_2)) packet.readLong()
        else if (version.moreOrEqual(Version.V1_8)) packet.readVarInt().toLong()
        else packet.readInt().toLong()
    }
}