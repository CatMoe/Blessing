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

package catmoe.fallencrystal.moefilter.api.command.impl.test

import catmoe.fallencrystal.moefilter.api.command.ICommand
import catmoe.fallencrystal.moefilter.util.message.v2.MessageUtil
import catmoe.fallencrystal.moefilter.util.message.v2.packet.type.MessagesType
import catmoe.fallencrystal.moefilter.util.plugin.util.Scheduler
import catmoe.fallencrystal.translation.command.annotation.MoeCommand
import catmoe.fallencrystal.translation.command.annotation.misc.DescriptionType
import catmoe.fallencrystal.translation.utils.config.LocalConfig
import catmoe.fallencrystal.translation.utils.webhook.WebhookSender
import net.md_5.bungee.api.CommandSender
import java.awt.Color

/*
@DebugCommand
@CommandDescription(DescriptionFrom.STRING, "Send a test webhook")
@ConsoleCanExecute
 */
@Suppress("SpellCheckingInspection")
@MoeCommand(
    name = "testwebhook",
    permission = "moefilter.testwebhook",
    descType = DescriptionType.STRING,
    descValue = "Send a test webhook",
    usage = ["/moefilter testwebhook"],
    debug = true,
    allowConsole = true,
)
class TestWebhookCommand : ICommand {
    override fun execute(sender: CommandSender, args: Array<out String>) {
        Scheduler.getDefault().runAsync {
            val conf = LocalConfig.getConfig().getConfig("notifications.webhook.test")
            if (!conf.getBoolean("enabled")) { MessageUtil.sendMessage("<red>Rejected send request because this webhook is disabled", MessagesType.ACTION_BAR, sender); return@runAsync }
            val url = conf.getString("url")
            if (url.isEmpty()) { MessageUtil.sendMessage("<red>URL is empty!", MessagesType.ACTION_BAR, sender); return@runAsync }
            val embedColor = Color(conf.getInt("embed.color.r"), conf.getInt("embed.color.g"), conf.getInt("embed.color.b"))
            try { WebhookSender().sendWebhook(conf.getStringList("format").joinToString("\n"), conf.getString("title"), embedColor, conf.getBoolean("embed.enabled"), url, conf.getString("ping"), conf.getString("username")) }
            catch (e: Exception) { MessageUtil.sendMessage("<red>Error when sending webhook: ${e.localizedMessage}. To Get More information, Please check console.", MessagesType.CHAT, sender); e.printStackTrace(); return@runAsync }
            MessageUtil.sendMessage("<green>Webhook looks are send successfully.", MessagesType.ACTION_BAR, sender)
        }
    }

    override fun tabComplete(sender: CommandSender, args: Array<out String>): MutableCollection<String>? {
        return null
    }
}