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
import io.netty.buffer.ByteBuf

@Suppress("MemberVisibilityCanBePrivate")
class PacketPluginRequest : LimboS2CPacket() {

    val messageId: Int? = null
    val channel: String? = null
    val data: ByteBuf? = null

    override fun encode(packet: ByteMessage, version: Version?) {
        packet.writeVarInt(messageId ?: return)
        packet.writeString(this.channel)
        packet.writeBytes(data ?: return)
    }

    override fun toString(): String {
        return "messageId=$messageId, channel=$channel, data=$data"
    }
}