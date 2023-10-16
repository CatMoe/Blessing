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