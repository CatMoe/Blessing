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

package catmoe.fallencrystal.moefilter.util.message.v2.processor

import catmoe.fallencrystal.translation.utils.component.ComponentUtil
import catmoe.fallencrystal.moefilter.util.message.v2.MessageUtil
import catmoe.fallencrystal.moefilter.util.message.v2.packet.MessagePacket
import net.kyori.adventure.text.Component
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.chat.ComponentSerializer

@Suppress("MemberVisibilityCanBePrivate")
abstract class AbstractMessageProcessor : IMessagePacketProcessor {

    fun serializerToString(component: Component): String { return ComponentUtil.toGson(component) }

    fun getComponent(packet: MessagePacket?, message: String): Component {
        //return if (packet?.getBaseComponent() != null) packet.getBaseComponent() else colorize(message)
        return packet?.getComponent() ?: ComponentUtil.parse(message, true)
    }

    fun getSerializer(packet: MessagePacket?, component: Component): String {
        // return if (packet?.getComponentSerializer() != null) packet.getComponentSerializer() else serializerToString(baseComponent)
        return packet?.getComponentSerializer() ?: serializerToString(component)
    }

    fun getLegacyComponent(packet: MessagePacket?, message: String): BaseComponent {
        // return if (packet?.getLegacyComponent() != null) packet.getLegacyComponent() else colorize(message, false)
        return packet?.getLegacyComponent() ?: MessageUtil.colorize(message, false)
    }

    fun getLegacySerializer(packet: MessagePacket?, component: BaseComponent): String { return packet?.getLegacySerializer() ?: ComponentSerializer.toString(component) }

}