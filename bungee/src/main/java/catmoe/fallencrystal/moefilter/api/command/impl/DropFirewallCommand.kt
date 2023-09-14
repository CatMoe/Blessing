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
import catmoe.fallencrystal.moefilter.common.firewall.Firewall
import catmoe.fallencrystal.moefilter.util.message.v2.MessageUtil
import catmoe.fallencrystal.moefilter.util.message.v2.packet.type.MessagesType
import catmoe.fallencrystal.translation.command.annotation.MoeCommand
import catmoe.fallencrystal.translation.command.annotation.misc.DescriptionType
import catmoe.fallencrystal.translation.utils.config.LocalConfig
import net.md_5.bungee.api.CommandSender
import java.net.InetAddress
import java.net.UnknownHostException

/*
@Command("drop")
@CommandDescription(DescriptionFrom.MESSAGE_PATH, "drop-command.description")
@CommandPermission("moefilter.drop")
@CommandUsage(["/moefilter drop"])
@ConsoleCanExecute
 */
@MoeCommand(
    name = "drop",
    permission = "moefilter.drop",
    descType = DescriptionType.MESSAGE_CONFIG,
    descValue = "drop-command.description",
    usage = ["/moefilter drop", "/moefilter drop <Address>"],
    allowConsole = true,
)
class DropFirewallCommand : ICommand {
    override fun execute(sender: CommandSender, args: Array<out String>) {
        if (args.size < 2) {
            Firewall.cache.invalidateAll()
            Firewall.tempCache.invalidateAll()
            sendMessage(sender, "dropped-all")
        } else {
            val raw = MessageUtil.argsBuilder(1, args).toString()
            try {
                val address = InetAddress.getByName(raw)
                if (Firewall.isFirewalled(address)) {
                    Firewall.cache.invalidate(address)
                    Firewall.tempCache.invalidate(address)
                    sendMessage(sender, "dropped")
                } else {
                    sendMessage(sender, "not-found")
                }
            } catch (_: UnknownHostException) {
                sendMessage(sender, "invalid-address")
            }
        }
    }

    private fun sendMessage(sender: CommandSender, key: String) {
        val conf = LocalConfig.getMessage()
        MessageUtil.sendMessage("${conf.getString("prefix")}${conf.getString("drop-command.$key")}", MessagesType.CHAT, sender)
    }

    override fun tabComplete(sender: CommandSender): MutableMap<Int, List<String>> { return mutableMapOf() }
}