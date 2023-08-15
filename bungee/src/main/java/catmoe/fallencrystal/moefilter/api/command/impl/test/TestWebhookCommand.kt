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

package catmoe.fallencrystal.moefilter.api.command.impl.test

import catmoe.fallencrystal.moefilter.MoeFilterBungee
import catmoe.fallencrystal.moefilter.api.command.ICommand
import catmoe.fallencrystal.moefilter.api.command.annotation.CommandDescription
import catmoe.fallencrystal.moefilter.api.command.annotation.ConsoleCanExecute
import catmoe.fallencrystal.moefilter.api.command.annotation.DebugCommand
import catmoe.fallencrystal.moefilter.api.command.annotation.misc.DescriptionFrom
import catmoe.fallencrystal.translation.utils.config.LocalConfig
import catmoe.fallencrystal.moefilter.common.utils.webhook.WebhookSender
import catmoe.fallencrystal.moefilter.util.message.v2.MessageUtil
import catmoe.fallencrystal.moefilter.util.message.v2.packet.type.MessagesType
import catmoe.fallencrystal.moefilter.util.plugin.util.Scheduler
import net.md_5.bungee.api.CommandSender
import java.awt.Color

@DebugCommand
@CommandDescription(DescriptionFrom.STRING, "Send a test webhook")
@ConsoleCanExecute
class TestWebhookCommand : ICommand {
    override fun execute(sender: CommandSender, args: Array<out String>) {
        Scheduler(MoeFilterBungee.instance).runAsync {
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

    override fun tabComplete(sender: CommandSender): MutableMap<Int, List<String>> { return mutableMapOf() }
}