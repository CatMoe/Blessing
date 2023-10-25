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
import net.kyori.adventure.text.Component
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.protocol.packet.Chat
import net.md_5.bungee.protocol.packet.SystemChat

@Suppress("MemberVisibilityCanBePrivate")
class MessageChatPacket(
    val v119: SystemChat?,
    val legacy: Chat?,
    val legacy2: Chat?,
    @JvmField
    val component: Component,
    val gson: String,
    @JvmField
    val legacyComponent: BaseComponent,
    val legacyGson: String,
    val originalMessage: String
) : MessagePacket {
    override fun getType(): MessagesType { return MessagesType.CHAT }

    override fun supportChecker(version: Int): Boolean {
        val v = Version.of(version)
        if (v119 != null && v.moreOrEqual(Version.V1_19)) return true
        if (legacy != null && v.fromTo(Version.V1_16, Version.V1_18_2)) return true
        //return hasLegacy2Data && version > Version.V1_7_6.number
        if (legacy2 != null && v.fromTo(Version.V1_7_6, Version.V1_15_2)) return true
        return false
    }


    override fun getComponent(): Component { return component }

    override fun getComponentSerializer(): String { return gson }

    override fun getOriginal(): String { return originalMessage }

    override fun getLegacyComponent(): BaseComponent { return legacyComponent }

    override fun getLegacySerializer(): String { return legacyGson }
}