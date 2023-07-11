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

package catmoe.fallencrystal.moefilter.util.message.component

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.chat.ComponentSerializer

@Suppress("unused")
object ComponentUtil {
    fun toBaseComponents(component: Component): BaseComponent { return TextComponent(*ComponentSerializer.parse(toGson(component))) }

    fun legacyToComponent(legacy: String): Component { return LegacyComponentSerializer.legacySection().deserialize(ChatColor.translateAlternateColorCodes('&', legacy))
    }

    fun componentToRaw(component: Component): String { return MiniMessage.builder().strict(true).build().serialize(component) }

    private fun toGson(component: Component): String { return GsonComponentSerializer.gson().serialize(component) }

    fun parse(str: String): Component { return MiniMessage.miniMessage().deserialize(str) }
}
