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

package catmoe.fallencrystal.moefilter.network.bungee.limbo.packet.c2s

import catmoe.fallencrystal.moefilter.network.bungee.limbo.LimboHandler
import catmoe.fallencrystal.moefilter.network.bungee.limbo.packet.ByteMessage
import catmoe.fallencrystal.moefilter.network.bungee.limbo.packet.LimboC2SPacket
import catmoe.fallencrystal.moefilter.network.bungee.limbo.util.Version
import catmoe.fallencrystal.moefilter.network.bungee.limbo.util.handshake.Protocol
import io.netty.channel.Channel

class PacketHandshake : LimboC2SPacket() {

    var version: Version = Version.min
    var nextState = Protocol.HANDSHAKING
    private var handler: LimboHandler? = null
    var host = "localhost"
    var port = 25565

    override fun decode(packet: ByteMessage, channel: Channel, version: Version?) {
        this.version = Version.of(packet.readVarInt())
        nextState = Protocol.STATE_BY_ID[packet.readVarInt()]!!
        this.host = packet.readString(packet.readVarInt())
        this.port = packet.readUnsignedShort()
    }

    override fun handle(handler: LimboHandler) {
        this.handler=handler
        handler.packetHandler.handle(handler, this)
    }
}