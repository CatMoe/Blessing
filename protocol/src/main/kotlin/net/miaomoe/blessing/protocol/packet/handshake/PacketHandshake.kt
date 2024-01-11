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
            /* Legacy FML */.removeSuffix("FML")
            /* SRV解析后缀 */.removeSuffix(".")
        // 检查域名是否有效
        require(host.contains(".") && host.length in 4..253) { "Invalid hostname!" }
        this.port = byteBuf.readUnsignedShort() // 读取端口
        // 检查端口号范围
        require(port in 1..65535) { "Port must be higher than 0 and lower than 65536" }
        nextState = when (byteBuf.readVarInt()) {
            1 -> State.STATUS // 请求motd
            2 -> State.LOGIN // 登录
            else -> throw IllegalArgumentException("Handshake state must be 1 or 2!")
        }
    }

    override fun toString() = "${this::class.simpleName}(version=$version, nextState=$nextState, host=$host, port=$port)"
}