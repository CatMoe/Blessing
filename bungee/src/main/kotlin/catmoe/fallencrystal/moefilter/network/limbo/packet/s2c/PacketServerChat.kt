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
import catmoe.fallencrystal.moefilter.network.limbo.compat.message.NbtMessage
import catmoe.fallencrystal.moefilter.network.limbo.packet.LimboS2CPacket
import catmoe.fallencrystal.translation.utils.version.Version
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.chat.ComponentSerializer
import java.util.*

class PacketServerChat(
    var type: MessageType = MessageType.CHAT,
    var message: NbtMessage = NbtMessage.EMPTY,
    var sender: UUID = UUID(0, 0) // empty
) : LimboS2CPacket() {
    override fun encode(byteBuf: ByteMessage, version: Version?) {
        if (version!!.moreOrEqual(Version.V1_19)) {
            message.write(byteBuf, version)
            if (version.moreOrEqual(Version.V1_19_1))
                byteBuf.writeBoolean(type == MessageType.ACTION_BAR)
            else
                byteBuf.writeVarInt(
                    if (type == MessageType.ACTION_BAR)
                        MessageType.ACTION_BAR.ordinal
                    else
                        MessageType.SYSTEM.ordinal
                )
        } else {
            if (version.lessOrEqual(Version.V1_10) && type == MessageType.ACTION_BAR) {
                val deserialize = try {
                    BaseComponent.toLegacyText(ComponentSerializer.deserialize(message.json))
                } catch (_: NoSuchMethodError) {
                    BaseComponent.toLegacyText(*ComponentSerializer.parse(message.json))
                }
                byteBuf.writeString(
                    ComponentSerializer.toString(TextComponent(deserialize))
                )
            } else byteBuf.writeString(message.json)
            byteBuf.writeByte(type.ordinal)
            if (version.moreOrEqual(Version.V1_16)) byteBuf.writeUuid(sender)
        }
    }

    enum class MessageType {
        CHAT,
        SYSTEM,
        ACTION_BAR
    }

    override fun toString() = "PacketServerChat(type=$type, message=$message, sender=$sender)"
}