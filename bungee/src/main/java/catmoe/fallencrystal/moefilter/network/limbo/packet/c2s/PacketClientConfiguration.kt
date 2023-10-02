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

import catmoe.fallencrystal.moefilter.network.limbo.netty.ByteMessage
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