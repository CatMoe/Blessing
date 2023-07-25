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

package catmoe.fallencrystal.moefilter.network.bungee.limbo.netty

import catmoe.fallencrystal.moefilter.network.bungee.limbo.packet.ByteMessage
import catmoe.fallencrystal.moefilter.network.bungee.limbo.packet.LimboPacket
import catmoe.fallencrystal.moefilter.network.bungee.limbo.packet.PacketSnapshot
import catmoe.fallencrystal.moefilter.network.bungee.limbo.util.Version
import catmoe.fallencrystal.moefilter.network.bungee.limbo.util.handshake.HandshakeState
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder

class LimboEncoder(var version: Version?) : MessageToByteEncoder<LimboPacket>() {

    var registry = HandshakeState.HANDSHAKING.clientBound.registry[version ?: Version.min]

    fun switchVersion(version: Version, state: HandshakeState) {
        this.version=version
        registry= state.clientBound.registry[version]
    }

    override fun encode(ctx: ChannelHandlerContext, packet: LimboPacket?, out: ByteBuf?) {
        if (out == null) return
        val msg = ByteMessage(out)
        val packetId = if (packet is PacketSnapshot) registry!!.getPacketId(packet.wrappedPacket::class.java) else registry!!.getPacketId(packet!!::class.java)
        if (packetId != -1) msg.writeVarInt(packetId)
        try { packet.encode(msg, version) } catch (ex: Exception) { ex.printStackTrace() }
    }
}