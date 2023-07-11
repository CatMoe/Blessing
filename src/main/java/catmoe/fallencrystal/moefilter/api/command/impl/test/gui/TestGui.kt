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

package catmoe.fallencrystal.moefilter.api.command.impl.test.gui

import catmoe.fallencrystal.moefilter.util.message.component.ComponentUtil
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
    }

    fun colorize(text: String): Component { return ComponentUtil.parse(text) }
}