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
