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

package catmoe.fallencrystal.moefilter.network.limbo.netty

import catmoe.fallencrystal.moefilter.network.limbo.packet.LimboPacket
import catmoe.fallencrystal.moefilter.network.limbo.packet.handshake.PacketSnapshot
import catmoe.fallencrystal.moefilter.network.limbo.packet.handshake.Protocol
import catmoe.fallencrystal.moefilter.network.limbo.util.Version
import catmoe.fallencrystal.moefilter.util.message.v2.MessageUtil
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder

class LimboEncoder(var version: Version?) : MessageToByteEncoder<LimboPacket>() {

    private var registry = Protocol.HANDSHAKING.clientBound.registry[version ?: Version.min]

    fun switchVersion(version: Version, state: Protocol) {
        this.version=version
        registry = state.clientBound.registry[version]
        MessageUtil.logInfo("[MoeLimbo] Encoder mappings refreshed. Now switch to state ${state.name} for version ${version.name}")
    }

    override fun encode(ctx: ChannelHandlerContext, packet: LimboPacket?, out: ByteBuf?) {
        if (out == null || packet == null) return
        val msg = ByteMessage(out)
        val packetId = if (packet is PacketSnapshot) registry!!.getPacketId(packet.wrappedPacket::class.java) else registry!!.getPacketId(packet::class.java)
        if (packetId != -1) msg.writeVarInt(packetId)
        val packetClazz =
            try {
                if (packet is PacketSnapshot) registry!!.getPacket(registry!!.getPacketId(packet.wrappedPacket.javaClass)) else packet
            } catch (npe: NullPointerException) { null }
        val pn = packetClazz?.javaClass?.simpleName ?: "null"
        if (packetId == -1 || packetClazz == null) { MessageUtil.logWarn("[MoeLimbo] Cancelled for null packet"); return }
        try {
            packet.encode(msg, version)
            MessageUtil.logInfo(
                "[MoeLimbo] Encoding packet ${"0x%02X".format(packetId)} ($packetId) ($pn) " +
                    "for version ${(version ?: Version.UNDEFINED).name} with ${msg.readableBytes()} bytes length")
            MessageUtil.logInfo("[MoeLimbo] [Encoder] Packet data: $packetClazz")
        } catch (ex: Exception) { ex.printStackTrace() }
    }
}