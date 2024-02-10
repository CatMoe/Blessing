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

package net.miaomoe.blessing.protocol.packet.common

import net.miaomoe.blessing.protocol.direction.PacketDirection
import net.miaomoe.blessing.protocol.packet.type.PacketBidirectional
import net.miaomoe.blessing.protocol.util.ByteMessage
import net.miaomoe.blessing.protocol.version.Version

@Suppress("MemberVisibilityCanBePrivate")
class PacketClientConfiguration : PacketBidirectional {

    override val forceDirection = PacketDirection.TO_SERVER

    var rawLocale: String? = null
    var viewDistance: Byte? = null
    var chatMode: Int? = null
    var chatColor: Boolean? = null
    var displaySkinParts: Short? = null
    var mainHand: Int? = null
    var enableTextFiltering: Boolean? = null
    var allowServerListings: Boolean? = null

    var difficulty: Byte? = null

    override fun decode(byteBuf: ByteMessage, version: Version, direction: PacketDirection) {
        val rawLocale = byteBuf.readString()
        this.rawLocale = rawLocale
        val viewDistance = byteBuf.readByte()
        this.viewDistance = viewDistance
        require(viewDistance > 2) { "View distance cannot lower than 2!" }
        val chatMode = byteBuf.readVarInt()
        this.chatMode=chatMode
        require(chatMode in 0..2) { "chatMode must be in 0-2!" }
        chatColor = byteBuf.readBoolean()

        if (version.lessOrEqual(Version.V1_7_6)) difficulty= byteBuf.readByte()

        displaySkinParts=byteBuf.readUnsignedByte()
        if (version.moreOrEqual(Version.V1_9)) {
            val mainHand = byteBuf.readVarInt()
            this.mainHand=mainHand
            require(mainHand in 0..1) { "mainHand must be 0 or 1!" }
            if (version.moreOrEqual(Version.V1_17)) enableTextFiltering=byteBuf.readBoolean()
            if (version.moreOrEqual(Version.V1_18)) allowServerListings=byteBuf.readBoolean()
        }
    }

    override fun encode(byteBuf: ByteMessage, version: Version, direction: PacketDirection) {
        byteBuf.writeString(rawLocale)
        byteBuf.writeByte(viewDistance?.toInt() ?: -1)
        byteBuf.writeVarInt(chatMode ?: -1)
        byteBuf.writeBoolean(chatColor ?: false)
        if (version.lessOrEqual(Version.V1_7_6)) byteBuf.writeByte(difficulty?.toInt() ?: -1)
        byteBuf.writeShort(displaySkinParts?.toInt() ?: -1)
        if (version.moreOrEqual(Version.V1_9)) {
            byteBuf.writeVarInt(mainHand ?: -1)
            if (version.moreOrEqual(Version.V1_17)) byteBuf.writeBoolean(enableTextFiltering ?: false)
            if (version.moreOrEqual(Version.V1_18)) byteBuf.writeBoolean(allowServerListings ?: false)
        }
    }

    override fun toString(): String {
        val fields = this::class.java.declaredFields
        val map = mutableMapOf<String, Any>()
        for (field in fields) {
            field.isAccessible=true
            map[field.name] = field[this]
        }
        return map.map { (key, value) -> "$key=$value" }.joinToString(", ")
    }

}