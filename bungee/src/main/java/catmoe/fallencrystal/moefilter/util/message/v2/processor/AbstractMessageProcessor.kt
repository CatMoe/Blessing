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

package catmoe.fallencrystal.moefilter.util.message.v2.processor

import catmoe.fallencrystal.moefilter.common.utils.component.ComponentUtil
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