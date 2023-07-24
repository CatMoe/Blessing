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
import io.netty.buffer.ByteBuf
import io.netty.channel.Channel

@Suppress("MemberVisibilityCanBePrivate")
class PacketPluginRequest() : LimboS2CPacket() {

    val messageId: Int? = null
    val channel: String? = null
    val data: ByteBuf? = null

    override fun encode(packet: ByteMessage, channel: Channel, version: Version?) {
        packet.writeVarInt(messageId ?: return)
        packet.writeString(this.channel)
        packet.writeBytes(data ?: return)
    }
}