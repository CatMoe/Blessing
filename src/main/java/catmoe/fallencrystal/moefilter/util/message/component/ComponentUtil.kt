package catmoe.fallencrystal.moefilter.util.message.component

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.chat.ComponentSerializer

object ComponentUtil {
    fun toBaseComponents(component: Component): BaseComponent { return TextComponent(*ComponentSerializer.parse(toGson(component))) }

    fun legacyToComponent(legacy: String): Component { return LegacyComponentSerializer.legacySection().deserialize(ChatColor.translateAlternateColorCodes('&', legacy)) as Component }

    fun ComponentToRaw(component: Component): String { return MiniMessage.builder().strict(true).build().serialize(component) }

    private fun toGson(component: Component): String { return GsonComponentSerializer.gson().serialize(component) }

    fun parse(str: String): Component { return MiniMessage.miniMessage().deserialize(str) }
}
