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

    //override fun tabComplete(sender: CommandSender): MutableMap<Int, List<String>> { return mutableMapOf() }

    override fun tabComplete(sender: CommandSender, args: Array<out String>): MutableCollection<String>? { return null }
}