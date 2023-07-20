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

import catmoe.fallencrystal.moefilter.util.message.component.ComponentUtil
import catmoe.fallencrystal.moefilter.util.message.v2.packet.MessagePacket
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.chat.ComponentSerializer

@Suppress("MemberVisibilityCanBePrivate")
abstract class AbstractMessageProcessor : IMessagePacketProcessor {

    fun colorize(string: String): BaseComponent { return ComponentUtil.toBaseComponents(ComponentUtil.parse(string)) }

    fun serializerToString(baseComponent: BaseComponent): String { return ComponentSerializer.toString(baseComponent) }

    fun getBaseComponent(packet: MessagePacket?, message: String): BaseComponent {
        return if (packet?.getBaseComponent() != null) packet.getBaseComponent() else colorize(message)
    }

    fun getSerializer(packet: MessagePacket?, baseComponent: BaseComponent): String {
        return if (packet?.getComponentSerializer() != null) packet.getComponentSerializer() else serializerToString(baseComponent)
    }

}