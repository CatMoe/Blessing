/*
 * Copyright (C) 2023-2023. CatMoe / MoeFilter Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package catmoe.fallencrystal.moefilter.network.limbo.packet.c2s

import catmoe.fallencrystal.moefilter.network.common.ByteMessage
import catmoe.fallencrystal.moefilter.network.common.exception.InvalidHandshakeException
import catmoe.fallencrystal.moefilter.network.limbo.compat.FakeInitialHandler
import catmoe.fallencrystal.moefilter.network.limbo.handler.LimboHandler
import catmoe.fallencrystal.moefilter.network.limbo.packet.LimboC2SPacket
import catmoe.fallencrystal.moefilter.network.limbo.packet.protocol.Protocol
import catmoe.fallencrystal.translation.utils.version.Version
import io.netty.channel.Channel
import java.net.InetSocketAddress

@Suppress("MemberVisibilityCanBePrivate")
class PacketHandshake : LimboC2SPacket() {

    var version: Version = Version.UNDEFINED
    var nextState = Protocol.HANDSHAKING
    var host = "localhost"
    var port = 25565

    override fun decode(byteBuf: ByteMessage, channel: Channel, version: Version?) {
        this.version = try { Version.of(byteBuf.readVarInt()) } catch (e: IllegalArgumentException) { Version.UNDEFINED }
        this.host = byteBuf.readString(byteBuf.readVarInt()).removeSuffix("FML").removeSuffix(".")
        // Legacy的FML客户端会在host后面加上FML标签 所以为253 + 3
        if (host.length >= 256 || host.isEmpty() || host == "0" /* Prevent EndMinecraftPlus ping ——They host is still 0 */)
            throw InvalidHandshakeException("Host length check failed")
        this.port = byteBuf.readUnsignedShort()
        if (port <= 0 || port > 65535) throw InvalidHandshakeException("Port must be higher than 0 and lower than 65535 (Non-vanilla?)")
        val state = byteBuf.readVarInt()
        if (state != 1 && state != 2) throw InvalidHandshakeException("Handshake state cannot lower than 1 or high than 2! (Non-vanilla?)")
        nextState = Protocol.STATE_BY_ID[state] ?: throw InvalidHandshakeException("Cannot found this state!")
    }

    override fun handle(handler: LimboHandler) {
        handler.host = InetSocketAddress(host, port)
        handler.updateVersion(version, nextState)
        if (handler.fakeHandler is FakeInitialHandler) handler.fakeHandler.connectionFrom=handler.host
    }

    override fun toString(): String {
        return "PacketHandshake(version=$version, nextState=$nextState, host=$host, port=$port)"
    }
}