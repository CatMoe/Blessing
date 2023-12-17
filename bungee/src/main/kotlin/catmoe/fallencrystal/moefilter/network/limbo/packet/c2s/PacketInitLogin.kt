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
import catmoe.fallencrystal.moefilter.network.common.exception.InvalidUsernameException
import catmoe.fallencrystal.moefilter.network.limbo.compat.FakeInitialHandler
import catmoe.fallencrystal.moefilter.network.limbo.handler.LimboHandler
import catmoe.fallencrystal.moefilter.network.limbo.handler.LimboLoader
import catmoe.fallencrystal.moefilter.network.limbo.packet.LimboC2SPacket
import catmoe.fallencrystal.moefilter.network.limbo.packet.cache.EnumPacket
import catmoe.fallencrystal.moefilter.network.limbo.packet.protocol.Protocol
import catmoe.fallencrystal.translation.utils.version.Version
import io.netty.channel.Channel

class PacketInitLogin : LimboC2SPacket() {

    var username = ""

    override fun decode(byteBuf: ByteMessage, channel: Channel, version: Version?) {
        username = byteBuf.readString(byteBuf.readVarInt())
        if (username == "") throw InvalidUsernameException("Username is empty!")
        //if (version!!.less(Version.V1_20_2) && !packet.readBoolean()) return
        //packet.readUuid()
    }

    override fun handle(handler: LimboHandler) {
        val profile = handler.profile
        profile.username=this.username
        profile.version=handler.version
        handler.sendPacket(handler.getCachedPacket(EnumPacket.LOGIN_SUCCESS))
        LimboLoader.connections.add(handler)
        if (handler.fakeHandler is FakeInitialHandler) handler.fakeHandler.username=profile.username
        if (handler.version.less(Version.V1_20_2)) {
            handler.updateVersion(handler.version, Protocol.PLAY)
            handler.sendPlayPackets()
        }
    }

    override fun toString(): String { return "PacketInitLogin(username=$username)" }
}