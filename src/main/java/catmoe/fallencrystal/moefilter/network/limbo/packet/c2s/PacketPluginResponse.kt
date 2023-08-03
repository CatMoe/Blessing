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

package catmoe.fallencrystal.moefilter.network.limbo.packet.c2s

import catmoe.fallencrystal.moefilter.network.limbo.netty.ByteMessage
import catmoe.fallencrystal.moefilter.network.limbo.packet.LimboC2SPacket
import catmoe.fallencrystal.moefilter.network.limbo.util.Version
import io.netty.channel.Channel

@Suppress("MemberVisibilityCanBePrivate")
class PacketPluginResponse : LimboC2SPacket() {

    var messageId: Int? = null
    var successful: Boolean? = null
    var data: ByteMessage? = null

    override fun decode(packet: ByteMessage, channel: Channel, version: Version?) {
        messageId = packet.readVarInt()
        successful = packet.readBoolean()
        val rb = packet.readableBytes()
        if (rb > 0) { ByteMessage(packet.readBytes(rb)) }
    }

    override fun toString(): String { return "PacketPluginResponse(messageId=$messageId, successful=$successful, data=$data)" }
}