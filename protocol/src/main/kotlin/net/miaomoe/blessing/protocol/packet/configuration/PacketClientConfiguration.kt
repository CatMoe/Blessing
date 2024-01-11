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

package net.miaomoe.blessing.protocol.packet.configuration

import net.miaomoe.blessing.protocol.packet.type.PacketToServer
import net.miaomoe.blessing.protocol.util.ByteMessage
import net.miaomoe.blessing.protocol.version.Version
import java.util.*

@Suppress("MemberVisibilityCanBePrivate")
class PacketClientConfiguration : PacketToServer {

    var rawLocale: String? = null
    var locale: Locale? = null
    var viewDistance: Byte? = null
    var chatMode: Int? = null
    var chatColor: Boolean? = null
    var displaySkinParts: Short? = null
    var mainHand: Int? = null
    var enableTextFiltering: Boolean? = null
    var allowServerListings: Boolean? = null

    override fun decode(byteBuf: ByteMessage, version: Version) {
        val rawLocale = byteBuf.readString()
        this.rawLocale = rawLocale
        val a = rawLocale.split("_")
        locale = Locale(a[0], a[1])
        val viewDistance = byteBuf.readByte()
        this.viewDistance = viewDistance
        require(viewDistance > 2) { "View distance cannot lower than 2!" }
        val chatMode = byteBuf.readVarInt()
        this.chatMode=chatMode
        require(chatMode in 0..2) { "chatMode must be in 0-2!" }
        chatColor = byteBuf.readBoolean() // ChatColor. Ignored
        displaySkinParts=byteBuf.readUnsignedByte() // Displayed Skin Parts. Ignored
        val mainHand = byteBuf.readVarInt()
        this.mainHand=mainHand
        require(mainHand in 0..1) { "mainHand must be 0 or 1!" }
        enableTextFiltering=byteBuf.readBoolean()
        allowServerListings=byteBuf.readBoolean()
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