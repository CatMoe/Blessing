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
package catmoe.fallencrystal.moefilter.network.bungee.limbo.packet.s2c

import catmoe.fallencrystal.moefilter.network.bungee.limbo.handshake.Version
import catmoe.fallencrystal.moefilter.network.bungee.limbo.packet.ByteMessage
import catmoe.fallencrystal.moefilter.network.bungee.limbo.packet.LimboS2CPacket
import io.netty.channel.Channel
import java.util.*

class PacketPlayerInfo : LimboS2CPacket() {
    private var gameMode = 3
    private var username = ""
    private var uuid: UUID? = null

    override fun encode(packet: ByteMessage, channel: Channel, version: Version?) {
        if (version!!.less(Version.V1_8)) {
            packet.writeString(username)
            packet.writeBoolean(true) // online
            packet.writeShort(0)
        } else {
            if (version.moreOrEqual(Version.V1_19_3)) {
                val actions = EnumSet.noneOf(Action::class.java)
                actions.addAll(listOf(Action.ADD_PLAYER, Action.UPDATE_LISTED, Action.UPDATE_GAMEMODE))
                packet.writeEnumSet(actions, Action::class.java)
                packet.writeVarInt(1) // Array length (1 element)
                packet.writeUuid(uuid!!) // entity uuid
                packet.writeString(username) // name
                packet.writeVarInt(0)
                packet.writeBoolean(true)
                packet.writeVarInt(gameMode)
                return
            }
            packet.writeVarInt(0)
            packet.writeVarInt(1)
            packet.writeUuid(uuid!!)
            packet.writeString(username)
            packet.writeVarInt(0)
            packet.writeVarInt(gameMode)
            packet.writeVarInt(60)
            packet.writeBoolean(false)
            if (version.moreOrEqual(Version.V1_19)) { packet.writeBoolean(false) }
        }
    }

    @Suppress("SpellCheckingInspection", "unused")
    enum class Action {
        ADD_PLAYER,
        INITIALIZE_CHAT,
        UPDATE_GAMEMODE,
        UPDATE_LISTED,
        UPDATE_LATENCY,
        UPDATE_DISPLAY_NAME
    }
}