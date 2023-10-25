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

package catmoe.fallencrystal.moefilter.api.command.impl

import catmoe.fallencrystal.moefilter.api.command.ICommand
import catmoe.fallencrystal.moefilter.event.PluginReloadEvent
import catmoe.fallencrystal.translation.command.annotation.MoeCommand
import catmoe.fallencrystal.translation.command.annotation.misc.DescriptionType
import catmoe.fallencrystal.translation.event.EventManager
import catmoe.fallencrystal.translation.executor.bungee.BungeeConsole
import catmoe.fallencrystal.translation.player.PlayerInstance
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.connection.ProxiedPlayer

/*
@Command("reload")
@ConsoleCanExecute
@CommandDescription(DescriptionFrom.MESSAGE_PATH, "command.description.reload")
@CommandUsage(["/moefilter reload"])
@CommandPermission("moefilter.reload")
 */
@MoeCommand(
    name = "reload",
    permission = "moefilter.reload",
    usage = ["/moefilter reload"],
    descType = DescriptionType.MESSAGE_CONFIG,
    descValue = "command.description.reload",
    allowConsole = true
)
class ReloadCommand : ICommand {

    override fun execute(sender: CommandSender, args: Array<out String>) {
        val executor = if (sender is ProxiedPlayer) PlayerInstance.getPlayer(sender.uniqueId) else BungeeConsole(sender)
        EventManager.callEvent(PluginReloadEvent(executor))
    }

    //override fun tabComplete(sender: CommandSender): MutableMap<Int, List<String>> { return mutableMapOf() }
    override fun tabComplete(sender: CommandSender, args: Array<out String>): MutableCollection<String>? { return null }
}