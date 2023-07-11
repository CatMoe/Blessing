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

import catmoe.fallencrystal.moefilter.api.command.ICommand
import catmoe.fallencrystal.moefilter.api.command.annotation.CommandDescription
import catmoe.fallencrystal.moefilter.api.command.annotation.CommandUsage
import catmoe.fallencrystal.moefilter.api.command.annotation.misc.DescriptionFrom
import dev.simplix.protocolize.data.inventory.InventoryType
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.connection.ProxiedPlayer

@CommandDescription(DescriptionFrom.STRING, "open a test gui")
@CommandUsage(["/moefilter gui"])
class TestGuiCommand : ICommand {
    override fun execute(sender: CommandSender, args: Array<out String>?) {
        val menu = TestGui()
        menu.setPlayer(sender as ProxiedPlayer)
        menu.type(InventoryType.GENERIC_9X3)
        menu.setTitle(menu.colorize("<gradient:green:yellow>a test gui</gradient>"))
        menu.open()
    }

    override fun tabComplete(sender: CommandSender): MutableMap<Int, List<String>> { return mutableMapOf() }
}