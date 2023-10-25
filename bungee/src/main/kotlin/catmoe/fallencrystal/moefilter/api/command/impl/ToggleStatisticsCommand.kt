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
import catmoe.fallencrystal.moefilter.util.message.notification.Notifications
import catmoe.fallencrystal.moefilter.util.message.v2.MessageUtil
import catmoe.fallencrystal.moefilter.util.message.v2.packet.type.MessagesType
import catmoe.fallencrystal.translation.command.annotation.MoeCommand
import catmoe.fallencrystal.translation.command.annotation.misc.DescriptionType
import catmoe.fallencrystal.translation.utils.config.LocalConfig
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.connection.ProxiedPlayer

/*
@Command("actionbar")
@CommandDescription(DescriptionFrom.MESSAGE_PATH, "statistics.command.actionbar.description")
@CommandUsage(["/moefilter actionbar"])
@CommandPermission("moefilter.notification")
 */
@MoeCommand(
    name = "actionbar",
    permission = "moefilter.notification",
    usage = ["/moefilter actionbar"],
    descType = DescriptionType.MESSAGE_CONFIG,
    descValue = "statistics.command.actionbar.description",
)
// ConsoleCanExecute / allowConsole = false is not here. so only can execute this command by who online player has permission.
class ToggleStatisticsCommand : ICommand {
    private val config = LocalConfig.getMessage()
    private val prefix = config.getString("prefix")
    private val enable = config.getString("statistics.command.actionbar.enable")
    private val disable = config.getString("statistics.command.actionbar.disable")

    override fun execute(sender: CommandSender, args: Array<out String>) {
        if (Notifications.autoNotification.contains(sender as ProxiedPlayer)) {
            Notifications.autoNotification.remove(sender)
            MessageUtil.sendMessage("$prefix$disable", MessagesType.CHAT, sender)
            return
        }
        if (Notifications.switchNotification.contains(sender)) {
            Notifications.switchNotification.remove(sender)
            MessageUtil.sendMessage("$prefix$disable", MessagesType.CHAT, sender)
        } else {
            Notifications.switchNotification.add(sender)
            MessageUtil.sendMessage("$prefix$enable", MessagesType.CHAT, sender)
        }
    }

    //override fun tabComplete(sender: CommandSender): MutableMap<Int, List<String>> { return mutableMapOf() }
    override fun tabComplete(sender: CommandSender, args: Array<out String>): MutableCollection<String>? { return null }
}