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
package catmoe.fallencrystal.moefilter.network.limbo.packet.s2c

import catmoe.fallencrystal.moefilter.network.limbo.netty.ByteMessage
import catmoe.fallencrystal.moefilter.network.limbo.packet.LimboS2CPacket
import catmoe.fallencrystal.moefilter.network.limbo.util.Version
import java.nio.charset.StandardCharsets
import java.util.*

class PacketPlayerInfo : LimboS2CPacket() {
    var gameMode = 2
    var username = "MoeLimbo"
    var uuid: UUID = UUID.nameUUIDFromBytes("OfflinePlayer:$username".toByteArray(StandardCharsets.UTF_8))

    override fun encode(packet: ByteMessage, version: Version?) {
        if (version!!.less(Version.V1_8)) {
            packet.writeString(username)
            packet.writeBoolean(true) // online
            packet.writeShort(0)
        } else {
            if (version.moreOrEqual(Version.V1_19_3)) {
                val actions = EnumSet.noneOf(Action::class.java)
                actions.add(Action.ADD_PLAYER)
                actions.add(Action.UPDATE_LISTED)
                actions.add(Action.UPDATE_GAMEMODE)
                packet.writeEnumSet(actions, Action::class.java)
                packet.writeVarInt(1) // Array length (1 element)
                packet.writeUuid(uuid) // entity uuid
                packet.writeString(username) // name
                packet.writeVarInt(0)
                packet.writeBoolean(true)
                packet.writeVarInt(gameMode)
                return
            }
            packet.writeVarInt(0)
            packet.writeVarInt(1)
            packet.writeUuid(uuid)
            packet.writeString(username)
            packet.writeVarInt(0)
            packet.writeVarInt(gameMode)
            packet.writeVarInt(60)
            packet.writeBoolean(false)
            if (version.moreOrEqual(Version.V1_19)) packet.writeBoolean(false)
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

    override fun toString(): String {
        return "PacketPlayerInfo(gameMode=$gameMode, username=$username, uuid=$uuid)"
    }
}