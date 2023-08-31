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

    override fun tabComplete(sender: CommandSender): MutableMap<Int, List<String>> { return mutableMapOf() }
}