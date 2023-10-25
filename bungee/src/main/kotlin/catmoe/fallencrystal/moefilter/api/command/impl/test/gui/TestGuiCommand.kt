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

import catmoe.fallencrystal.moefilter.api.command.ICommand
import catmoe.fallencrystal.translation.command.annotation.MoeCommand
import catmoe.fallencrystal.translation.command.annotation.misc.DescriptionType
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.connection.ProxiedPlayer

/*
@CommandDescription(DescriptionFrom.STRING, "open a test gui")
@CommandUsage(["/moefilter gui"])
@DebugCommand
 */
@Suppress("SpellCheckingInspection")
@MoeCommand(
    name = "testgui",
    permission = "moefilter.testgui",
    descType = DescriptionType.STRING,
    descValue = "open a test gui",
    debug = true
)
class TestGuiCommand : ICommand {
    override fun execute(sender: CommandSender, args: Array<out String>) {
        ProxyServer.getInstance().pluginManager.getPlugin("Protocolize") ?: return
        val menu = TestGui()
        menu.setPlayer(sender as ProxiedPlayer)
        menu.type(dev.simplix.protocolize.data.inventory.InventoryType.GENERIC_9X3)
        menu.setTitle(menu.colorize("<gradient:green:yellow>a test gui</gradient>"))
        menu.open()
    }

    override fun tabComplete(sender: CommandSender, args: Array<out String>): MutableCollection<String>? {
        return null
    }
}