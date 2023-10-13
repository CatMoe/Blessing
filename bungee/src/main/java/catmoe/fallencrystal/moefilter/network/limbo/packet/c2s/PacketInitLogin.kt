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

import catmoe.fallencrystal.moefilter.network.common.exception.InvalidUsernameException
import catmoe.fallencrystal.moefilter.network.limbo.compat.FakeInitialHandler
import catmoe.fallencrystal.moefilter.network.limbo.handler.LimboHandler
import catmoe.fallencrystal.moefilter.network.limbo.handler.MoeLimbo
import catmoe.fallencrystal.moefilter.network.common.ByteMessage
import catmoe.fallencrystal.moefilter.network.limbo.packet.LimboC2SPacket
import catmoe.fallencrystal.moefilter.network.limbo.packet.cache.EnumPacket
import catmoe.fallencrystal.moefilter.network.limbo.packet.protocol.Protocol
import catmoe.fallencrystal.translation.utils.version.Version
import io.netty.channel.Channel

class PacketInitLogin : LimboC2SPacket() {

    var username = ""

    override fun decode(packet: ByteMessage, channel: Channel, version: Version?) {
        username = packet.readString(packet.readVarInt())
        if (username == "") throw InvalidUsernameException("Username is empty!")
        //if (version!!.less(Version.V1_20_2) && !packet.readBoolean()) return
        //packet.readUuid()
    }

    override fun handle(handler: LimboHandler) {
        val profile = handler.profile
        profile.username=this.username
        profile.version=handler.version
        handler.sendPacket(handler.getCachedPacket(EnumPacket.LOGIN_SUCCESS))
        MoeLimbo.connections.add(handler)
        if (handler.fakeHandler is FakeInitialHandler) handler.fakeHandler.username=profile.username
        if (handler.version!!.less(Version.V1_20_2)) {
            handler.state = Protocol.PLAY
            handler.updateVersion(handler.version!!, handler.state!!)
            handler.sendPlayPackets()
        }
    }

    override fun toString(): String { return "PacketInitLogin(username=$username)" }
}