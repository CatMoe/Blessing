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

import catmoe.fallencrystal.moefilter.network.limbo.handler.LimboHandler
import catmoe.fallencrystal.moefilter.network.limbo.netty.ByteMessage
import catmoe.fallencrystal.moefilter.network.limbo.packet.LimboC2SPacket
import catmoe.fallencrystal.moefilter.network.limbo.packet.protocol.Protocol
import catmoe.fallencrystal.moefilter.network.limbo.util.Version
import io.netty.channel.Channel
import java.net.InetSocketAddress

class PacketHandshake : LimboC2SPacket() {

    var version: Version = Version.UNDEFINED
    var nextState = Protocol.HANDSHAKING
    var host = "localhost"
    var port = 25565

    override fun decode(packet: ByteMessage, channel: Channel, version: Version?) {
        this.version = try { Version.of(packet.readVarInt()) } catch (e: IllegalArgumentException) { Version.UNDEFINED }
        this.host = packet.readString(packet.readVarInt())
        this.port = packet.readUnsignedShort()
        nextState = Protocol.STATE_BY_ID[packet.readVarInt()]!!
    }

    override fun handle(handler: LimboHandler) {
        if (host.endsWith("FML")) { host.replace("FML", "") }
        handler.host = InetSocketAddress(host, port)
        handler.updateVersion(version, nextState)
    }

    override fun toString(): String {
        return "version=$version, nextState=$nextState, host=$host, port=$port"
    }
}