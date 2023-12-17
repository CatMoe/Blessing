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
package catmoe.fallencrystal.moefilter.network.limbo.packet.s2c

import catmoe.fallencrystal.moefilter.network.common.ByteMessage
import catmoe.fallencrystal.moefilter.network.limbo.packet.LimboS2CPacket
import catmoe.fallencrystal.translation.utils.version.Version
import java.nio.charset.StandardCharsets
import java.util.*

class PacketPlayerInfo(
    var gameMode: Int = 2,
    var username: String = "MoeLimbo",
    var uuid: UUID = UUID.nameUUIDFromBytes("OfflinePlayer:$username".toByteArray(StandardCharsets.UTF_8))
) : LimboS2CPacket() {

    override fun encode(byteBuf: ByteMessage, version: Version?) {
        if (version!!.less(Version.V1_8)) {
            byteBuf.writeString(username)
            byteBuf.writeBoolean(true) // online
            byteBuf.writeShort(0)
        } else {
            if (version.moreOrEqual(Version.V1_19_3)) {
                val actions = EnumSet.noneOf(Action::class.java)
                actions.add(Action.ADD_PLAYER)
                actions.add(Action.UPDATE_LISTED)
                actions.add(Action.UPDATE_GAMEMODE)
                byteBuf.writeEnumSet(actions, Action::class.java)
                byteBuf.writeVarInt(1) // Array length (1 element)
                byteBuf.writeUuid(uuid, version) // entity uuid
                byteBuf.writeString(username) // name
                byteBuf.writeVarInt(0)
                byteBuf.writeBoolean(true)
                byteBuf.writeVarInt(gameMode)
                return
            }
            byteBuf.writeVarInt(0)
            byteBuf.writeVarInt(1)
            byteBuf.writeUuid(uuid, version)
            byteBuf.writeString(username)
            byteBuf.writeVarInt(0)
            byteBuf.writeVarInt(gameMode)
            byteBuf.writeVarInt(60)
            byteBuf.writeBoolean(false)
            if (version.moreOrEqual(Version.V1_19)) byteBuf.writeBoolean(false)
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

    override fun toString() = "PacketPlayerInfo(gameMode=$gameMode, username=$username, uuid=$uuid)"
}