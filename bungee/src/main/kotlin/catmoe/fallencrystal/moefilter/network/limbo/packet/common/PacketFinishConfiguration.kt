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

package catmoe.fallencrystal.moefilter.network.limbo.packet.common

import catmoe.fallencrystal.moefilter.network.limbo.handler.LimboHandler
import catmoe.fallencrystal.moefilter.network.common.ByteMessage
import catmoe.fallencrystal.moefilter.network.limbo.packet.LimboPacket
import catmoe.fallencrystal.moefilter.network.limbo.packet.protocol.Protocol
import catmoe.fallencrystal.translation.utils.version.Version
import io.netty.channel.Channel

class PacketFinishConfiguration : LimboPacket {
    override fun encode(packet: ByteMessage, version: Version?) {
        // This packet does not have any field.
    }

    override fun decode(packet: ByteMessage, channel: Channel, version: Version?) {
        // This packet does not have any field.
    }

    override fun handle(handler: LimboHandler) {
        handler.state = Protocol.PLAY
        handler.updateVersion(handler.version!!, Protocol.PLAY)
        handler.sendPlayPackets()
    }
}