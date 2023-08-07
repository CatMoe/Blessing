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

import catmoe.fallencrystal.moefilter.network.common.ExceptionCatcher
import catmoe.fallencrystal.moefilter.network.limbo.handler.LimboHandler
import catmoe.fallencrystal.moefilter.network.limbo.handler.MoeLimbo
import catmoe.fallencrystal.moefilter.network.limbo.listener.LimboListener
import catmoe.fallencrystal.moefilter.network.limbo.packet.LimboPacket
import catmoe.fallencrystal.moefilter.network.limbo.packet.cache.PacketSnapshot
import catmoe.fallencrystal.moefilter.network.limbo.packet.protocol.Protocol
import catmoe.fallencrystal.moefilter.network.limbo.util.Version
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder

class LimboEncoder(var version: Version?) : MessageToByteEncoder<LimboPacket>() {

    private var registry = Protocol.HANDSHAKING.clientBound.registry[version ?: Version.min]
    var handler: LimboHandler? = null

    fun switchVersion(version: Version, state: Protocol) {
        this.version=version
        registry = state.clientBound.registry[version]
        MoeLimbo.debug("Encoder state changed. Version: ${version.name} State: ${state.name}")
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
        if (packetId == -1 || packetClazz == null) return
        try {
            if (LimboListener.handleSend(packetClazz, handler)) return
            packet.encode(msg, version)
            MoeLimbo.debug("Encoding ${"0x%02X".format(packetId)} packet with ${msg.readableBytes()}")
            MoeLimbo.debug(packetClazz.toString())
        } catch (ex: Exception) { ex.printStackTrace() }
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) { ExceptionCatcher.handle(ctx.channel(), cause) }
}