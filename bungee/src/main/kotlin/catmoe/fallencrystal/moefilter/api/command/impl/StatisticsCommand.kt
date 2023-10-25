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
import catmoe.fallencrystal.moefilter.common.state.StateManager
import catmoe.fallencrystal.moefilter.util.message.notification.Notifications
import catmoe.fallencrystal.moefilter.util.message.v2.MessageUtil
import catmoe.fallencrystal.moefilter.util.message.v2.packet.type.MessagesType
import catmoe.fallencrystal.translation.command.annotation.MoeCommand
import catmoe.fallencrystal.translation.command.annotation.misc.DescriptionType
import catmoe.fallencrystal.translation.utils.config.LocalConfig
import net.md_5.bungee.api.CommandSender

/*
@ConsoleCanExecute
@CommandDescription(DescriptionFrom.MESSAGE_PATH, "statistics.command.chat-description")
@Command("status")
@CommandUsage(["/moefilter status"])
 */
@MoeCommand(
    name = "status",
    permission = "moefilter.notification",
    usage = ["/moefilter status"],
    descType = DescriptionType.MESSAGE_CONFIG,
    descValue = "statistics.command.chat-description",
    allowConsole = true
)
class StatisticsCommand : ICommand {
    override fun execute(sender: CommandSender, args: Array<out String>) {
        if (args.size < 2 || args.size > 3) {
            val conf = LocalConfig.getMessage().getConfig("statistics")
            val message = Notifications.placeholder((if (StateManager.inAttack.get()) conf.getStringList("chat-format.attack") else conf.getStringList("chat-format.idle")).joinToString("\n"))
            MessageUtil.sendMessage(message, MessagesType.CHAT, sender)
        }
    }

    override fun tabComplete(sender: CommandSender, args: Array<out String>): MutableCollection<String>? { return null }
}