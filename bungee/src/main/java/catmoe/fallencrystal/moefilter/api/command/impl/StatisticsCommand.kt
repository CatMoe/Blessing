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

    override fun tabComplete(sender: CommandSender): MutableMap<Int, List<String>> {
        return mutableMapOf()
    }
}