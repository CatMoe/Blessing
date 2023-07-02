package catmoe.fallencrystal.moefilter.api.command.impl.test.gui

import catmoe.fallencrystal.moefilter.util.message.component.ComponentUtil
import catmoe.fallencrystal.moefilter.util.plugin.protocolize.ItemBuilder
import catmoe.fallencrystal.moefilter.util.plugin.protocolize.MenuBuilder
import dev.simplix.protocolize.data.ItemType
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration

class TestGui : MenuBuilder() {
    override fun define() {
        setItem(0, ItemBuilder(ItemType.NETHER_STAR)
            .name(colorize("<reset><gradient:light_purple:aqua>Nether star"))
            .lore(colorize("<reset><gradient:light_purple:aqua>That is nether or universe?"))
            .build()
        )
    }

    fun colorize(text: String): Component { return ComponentUtil.parse(text) }
}