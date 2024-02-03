/*
 * Copyright (C) 2023-2024. CatMoe / Blessing Contributors
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

package net.miaomoe.blessing.protocol.packet.handshake

import net.miaomoe.blessing.protocol.packet.type.PacketToServer
import net.miaomoe.blessing.protocol.registry.State
import net.miaomoe.blessing.protocol.util.ByteMessage
import net.miaomoe.blessing.protocol.version.Version

@Suppress("MemberVisibilityCanBePrivate")
class PacketHandshake : PacketToServer {

    var version: Version = Version.UNDEFINED
    var nextState = State.HANDSHAKE
    var host = "localhost"
    var port = 25565

    override fun decode(byteBuf: ByteMessage, version: Version) {
        this.version = Version.of(byteBuf.readVarInt()) // 版本号
        this.host = byteBuf.readString() // 读取域名 (作为字符串)
        this.port = byteBuf.readUnsignedShort() // 读取端口
        nextState = when (val state = byteBuf.readVarInt()) {
            1 -> State.STATUS // 请求motd
            2 -> State.LOGIN // 登录
            else -> throw IllegalArgumentException("Handshake state must be 1 or 2! ($state)")
        }
    }

    override fun toString() = "${this::class.simpleName}(version=$version, nextState=$nextState, host=$host, port=$port)"
}