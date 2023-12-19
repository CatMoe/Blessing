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
        try {
            this.version = Version.of(byteBuf.readVarInt()) // 版本号
            this.host = byteBuf.readString() // 读取域名 (作为字符串)
                /* Legacy FML */.removeSuffix("FML")
                /* SRV解析后缀 */.removeSuffix(".")
            // 检查域名是否有效
            require(host.contains(".") && host.length in 4..253) { "Invalid hostname!" }
            this.port = byteBuf.readUnsignedShort() // 读取端口
            // 检查端口号范围
            require(port in 1..65535) { "Port must be higher than 0 and lower than 65536" }
            val state = byteBuf.readVarInt()
            require(state in 1..2) { "Handshake state must be 1 or 2!" }
            nextState = Protocol.STATE_BY_ID[state] ?: throw IllegalArgumentException("Cannot found this state!")
        } catch (exception: IllegalArgumentException) {
            throw InvalidHandshakeException(exception.localizedMessage)
        }
    }

    override fun handle(handler: LimboHandler) {
        handler.host = InetSocketAddress(host, port)
        handler.updateVersion(version, nextState)
        if (handler.fakeHandler is FakeInitialHandler) handler.fakeHandler.connectionFrom=handler.host
    }

    override fun toString() =
        "PacketHandshake(version=$version, nextState=$nextState, host=$host, port=$port)"
}