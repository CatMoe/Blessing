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

package net.miaomoe.blessing.protocol.packet.play

import net.miaomoe.blessing.nbt.chat.MixedComponent
import net.miaomoe.blessing.protocol.packet.type.PacketToClient
import net.miaomoe.blessing.protocol.util.ByteMessage
import net.miaomoe.blessing.protocol.util.ComponentUtil.toComponentFromJson
import net.miaomoe.blessing.protocol.util.ComponentUtil.toLegacyText
import net.miaomoe.blessing.protocol.version.Version
import java.util.*

@Suppress("MemberVisibilityCanBePrivate")
class PacketServerChat(
    var message: MixedComponent = MixedComponent.EMPTY,
    var type: Type = Type.CHAT,
    var sender: UUID = UUID(0, 0)
) : PacketToClient {

    override fun encode(byteBuf: ByteMessage, version: Version) {
        val isActionbar = type == Type.ACTION_BAR
        if (version.lessOrEqual(Version.V1_10) && isActionbar) {
            byteBuf.writeString(message.toComponentFromJson().toLegacyText())
        } else byteBuf.writeChat(message, version)
        when {
            version.moreOrEqual(Version.V1_19_1) -> byteBuf.writeBoolean(isActionbar)
            version.moreOrEqual(Version.V1_19) -> byteBuf.writeVarInt(if (isActionbar) 2 else 1)
            version.moreOrEqual(Version.V1_8) -> byteBuf.writeByte(type.ordinal)
        }
        if (version.fromTo(Version.V1_16, Version.V1_18_2)) byteBuf.writeUUID(sender)
    }

    enum class Type {
        CHAT,
        SYSTEM,
        ACTION_BAR,
    }

}