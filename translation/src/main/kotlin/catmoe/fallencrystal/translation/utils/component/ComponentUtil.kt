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

package catmoe.fallencrystal.translation.utils.component

import catmoe.fallencrystal.translation.TranslationLoader
import catmoe.fallencrystal.translation.platform.Platform
import catmoe.fallencrystal.translation.platform.ProxyPlatform
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer

@Suppress("unused")
object ComponentUtil {

    private val legacyBuilder = GsonComponentSerializer.builder().downsampleColors().emitLegacyHoverEvent().build()

    @Platform(ProxyPlatform.BUNGEE)
    private fun a(component: Component): net.md_5.bungee.api.chat.BaseComponent { return net.md_5.bungee.api.chat.TextComponent(*net.md_5.bungee.chat.ComponentSerializer.parse(toGson(component))) }

    fun toBaseComponents(component: Component): net.md_5.bungee.api.chat.BaseComponent? {
        return TranslationLoader.secureAccess(a(component))
    }

    fun legacyToComponent(legacy: String): Component { return LegacyComponentSerializer.legacySection().deserialize(legacy) }

    fun componentToRaw(component: Component): String { return MiniMessage.builder().strict(true).build().serialize(component) }

    fun toGson(component: Component): String { return GsonComponentSerializer.gson().serialize(component) }

    fun parse(str: String): Component { return MiniMessage.miniMessage().deserialize(str) }

    fun parse(str: String, hex: Boolean): Component {
        return when (hex) {
            true -> parse(str)
            false -> legacyBuilder.deserialize(toGson(parse(str)))
        }
    }
}
