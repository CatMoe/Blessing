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
import catmoe.fallencrystal.moefilter.network.limbo.packet.LimboC2SPacket
import catmoe.fallencrystal.translation.utils.version.Version
import com.google.common.base.Preconditions
import io.netty.channel.Channel
import java.util.*

@Suppress("MemberVisibilityCanBePrivate")
class PacketClientConfiguration : LimboC2SPacket() {

    var rawLocale: String? = null
    var locale: Locale? = null
    var viewDistance: Byte? = null
    var chatMode: Int? = null
    var chatColor: Boolean? = null
    var displaySkinParts: Short? = null
    var mainHand: Int? = null
    var enableTextFiltering: Boolean? = null
    var allowServerListings: Boolean? = null

    override fun decode(packet: ByteMessage, channel: Channel, version: Version?) {
        val rawLocale = packet.readString()
        this.rawLocale = rawLocale
        val a = rawLocale.split("_")
        locale = Locale(a[0], a[1])
        val viewDistance = packet.readByte()
        this.viewDistance = viewDistance
        //if (viewDistance < 2) throw IllegalArgumentException("View distance cannot lower than 2!")
        Preconditions.checkArgument(viewDistance > 2, "View distance cannot lower than 2!")
        val chatMode = packet.readVarInt()
        this.chatMode=chatMode
        Preconditions.checkArgument(!(chatMode < 0 || chatMode > 2), "ChatMode cannot lower than 0 or higher than 2!")
        chatColor = packet.readBoolean() // ChatColor. Ignored
        displaySkinParts=packet.readUnsignedByte() // Displayed Skin Parts. Ignored
        val mainHand = packet.readVarInt()
        this.mainHand=mainHand
        Preconditions.checkArgument(!(mainHand > 1 || mainHand < 0), "MainHand only can be 0 or 1!")
        enableTextFiltering=packet.readBoolean()
        allowServerListings=packet.readBoolean()
    }

    override fun toString(): String {
        return "PacketClientConfiguration(" +
                "rawLocale=$rawLocale, " +
                "Locale=${locale.toString()}, " +
                "ViewDistance=$viewDistance, " +
                "ChatMode=$chatMode, " +
                "ChatColor=$chatColor, " +
                "DisplaySkinParts=$displaySkinParts, " +
                "MainHand=$mainHand, " +
                "EnableTextFiltering=$enableTextFiltering, " +
                "AllowServerListings=$allowServerListings" +
                ")"
    }


}