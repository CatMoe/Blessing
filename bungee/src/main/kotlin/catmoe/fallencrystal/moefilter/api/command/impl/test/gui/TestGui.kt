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

package catmoe.fallencrystal.moefilter.api.command.impl.test.gui

import catmoe.fallencrystal.translation.utils.component.ComponentUtil
import catmoe.fallencrystal.moefilter.util.plugin.protocolize.ItemBuilder
import catmoe.fallencrystal.moefilter.util.plugin.protocolize.MenuBuilder
import dev.simplix.protocolize.data.ItemType
import net.kyori.adventure.text.Component

class TestGui : MenuBuilder() {
    override fun define() {
        setItem(0, ItemBuilder(ItemType.NETHER_STAR)
            .name(colorize("<reset><gradient:light_purple:aqua>Nether star"))
            .lore(colorize("<reset><gradient:light_purple:aqua>That is nether or universe?"))
            .build()
        )
        setItem(1, ItemBuilder(ItemType.BEACON)
            .name(colorize("<reset><aqua>A beacon"))
            .lore(colorize("<reset><aqua>It's brighter."))
            .build()
        )
    }

    fun colorize(text: String): Component { return ComponentUtil.parse(text) }
}