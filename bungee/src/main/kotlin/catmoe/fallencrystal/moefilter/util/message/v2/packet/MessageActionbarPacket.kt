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

package catmoe.fallencrystal.moefilter.util.message.v2.packet

import catmoe.fallencrystal.moefilter.util.message.v2.packet.type.MessagesType
import catmoe.fallencrystal.translation.utils.version.Version
import catmoe.fallencrystal.translation.utils.version.Version.*
import net.kyori.adventure.text.Component
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.protocol.packet.Chat
import net.md_5.bungee.protocol.packet.SystemChat
import net.md_5.bungee.protocol.packet.Title

@Suppress("MemberVisibilityCanBePrivate")
class MessageActionbarPacket(
    val v119: SystemChat?,
    val v117: Chat?,
    val v116: Title?,
    val v111: Title?,
    val v110: Chat?,
    @JvmField
    val component: Component,
    val gson: String,
    @JvmField
    val legacyComponent: BaseComponent,
    val legacyGson: String,
    val originalMessage: String,
) : MessagePacket {
    override fun getType(): MessagesType { return MessagesType.ACTION_BAR }

    override fun supportChecker(version: Int): Boolean {
        //if (has119Data && version >= ProtocolConstants.MINECRAFT_1_19) return true
        //if (has117Data && version > ProtocolConstants.MINECRAFT_1_17) return true
        //if (has111Data && version > ProtocolConstants.MINECRAFT_1_10) return true
        //return has110Data && version > ProtocolConstants.MINECRAFT_1_8
        val v = Version.of(version)
        if (v119 != null && v.moreOrEqual(V1_19)) return true
        if (v117 != null && v.fromTo(V1_17, V1_18_2)) return true
        if (v116 != null && v.fromTo(V1_16, V1_16_4)) return true
        if (v111 != null && v.fromTo(V1_10, V1_15_2)) return true
        if (v110 != null && v.fromTo(V1_7_6, V1_9_4)) return true
        //return (has110Data && v.moreOrEqual(V1_7_6))
        return false
    }

    override fun getComponent(): Component { return component }
    override fun getLegacyComponent(): BaseComponent { return legacyComponent }

    override fun getComponentSerializer(): String { return gson }
    override fun getLegacySerializer(): String { return legacyGson }

    override fun getOriginal(): String { return originalMessage }
}