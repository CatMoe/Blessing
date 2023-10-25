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
import catmoe.fallencrystal.moefilter.common.firewall.lockdown.LockdownManager
import catmoe.fallencrystal.moefilter.util.message.v2.MessageUtil
import catmoe.fallencrystal.moefilter.util.message.v2.packet.type.MessagesType
import catmoe.fallencrystal.translation.command.annotation.MoeCommand
import catmoe.fallencrystal.translation.command.annotation.misc.DescriptionType
import catmoe.fallencrystal.translation.utils.config.LocalConfig
import net.md_5.bungee.api.CommandSender
import java.net.InetAddress

/*
@CommandDescription(DescriptionFrom.MESSAGE_PATH, "lockdown.description")
@CommandUsage([
    "/moefilter lockdown toggle",
    "/moefilter lockdown add \"<Address>\"",
    "/moefilter lockdown remove \"<Address>\""
])
@ConsoleCanExecute
 */
@MoeCommand(
    name = "lockdown",
    permission = "moefilter.lockdown",
    usage = [
        "/moefilter lockdown toggle",
        "/moefilter lockdown add \"<Address>\"",
        "/moefilter lockdown remove \"<Address>\""
    ],
    descType = DescriptionType.MESSAGE_CONFIG,
    descValue = "lockdown.description",
    allowConsole = true
)
class LockdownCommand : ICommand {

    override fun execute(sender: CommandSender, args: Array<out String>) {
        val conf = LocalConfig.getMessage().getConfig("lockdown")
        if (args.size < 2 || args.size > 3) {
            val state = if (LockdownManager.state.get()) conf.getString("enabled") else conf.getString("disabled")
            val stateMessage = conf.getStringList("state-message").joinToString("\n").replace("[state]", state)
            MessageUtil.sendMessage(stateMessage, MessagesType.CHAT, sender)
            return
        }
        val prefix = LocalConfig.getMessage().getString("prefix")
        when (args[1]) {
            "toggle" -> {
                when (LockdownManager.state.get()) {
                    true -> {
                        LockdownManager.setLockdown(false)
                        MessageUtil.sendMessage("$prefix${conf.getString("toggle.disable")}", MessagesType.CHAT, sender)
                    }
                    false -> {
                        LockdownManager.setLockdown(true)
                        MessageUtil.sendMessage("$prefix${conf.getString("toggle.enable")}", MessagesType.CHAT, sender)
                    }
                }
            }
            "add" -> {
                val address: InetAddress? = try { InetAddress.getByName(args[2]) } catch (_: Exception) { null }
                if (address == null) { MessageUtil.sendMessage("$prefix${conf.getString("parse-error")}", MessagesType.CHAT, sender); return }
                if (LockdownManager.whitelistCache.getIfPresent(address) != null) {
                    MessageUtil.sendMessage("$prefix${conf.getString("already-added")}", MessagesType.CHAT, sender); return
                } else {
                    LockdownManager.whitelistCache.put(address, true)
                    MessageUtil.sendMessage("$prefix${conf.getString("add")}", MessagesType.CHAT, sender)
                }
            }
            "remove" -> {
                val address: InetAddress? = try { InetAddress.getByName(args[2]) } catch (_: Exception) { null }
                if (address == null) { MessageUtil.sendMessage("$prefix${conf.getString("parse-error")}", MessagesType.CHAT, sender); return }
                if (LockdownManager.whitelistCache.getIfPresent(address) != null) {
                    LockdownManager.whitelistCache.invalidate(address)
                    MessageUtil.sendMessage("$prefix${conf.getString("remove")}", MessagesType.CHAT, sender); return
                } else {
                    MessageUtil.sendMessage("$prefix${conf.getString("not-found")}", MessagesType.CHAT, sender)
                }
            }
        }
        /*
        if (args2.equals("toggle", ignoreCase = true)) {
            when (LockdownManager.state.get()) {
                true -> {
                    LockdownManager.setLockdown(false)
                    MessageUtil.sendMessage("$prefix${conf.getString("toggle.disable")}", MessagesType.CHAT, sender)
                }
                false -> {
                    LockdownManager.setLockdown(true)
                    MessageUtil.sendMessage("$prefix${conf.getString("toggle.enable")}", MessagesType.CHAT, sender)
                }
            }
        }
        if (args2.equals("add", ignoreCase = true)) {
            val address: InetAddress? = try { InetAddress.getByName(args[2]) } catch (_: Exception) { null }
            if (address == null) { MessageUtil.sendMessage("$prefix${conf.getString("parse-error")}", MessagesType.CHAT, sender); return }
            if (LockdownManager.whitelistCache.getIfPresent(address) != null) {
                MessageUtil.sendMessage("$prefix${conf.getString("already-added")}", MessagesType.CHAT, sender); return
            } else {
                LockdownManager.whitelistCache.put(address, true)
                MessageUtil.sendMessage("$prefix${conf.getString("add")}", MessagesType.CHAT, sender)
            }
        }
        if (args2.equals("remove", ignoreCase = true)) {
            val address: InetAddress? = try { InetAddress.getByName(args[2]) } catch (_: Exception) { null }
            if (address == null) { MessageUtil.sendMessage("$prefix${conf.getString("parse-error")}", MessagesType.CHAT, sender); return }
            if (LockdownManager.whitelistCache.getIfPresent(address) != null) {
                LockdownManager.whitelistCache.invalidate(address)
                MessageUtil.sendMessage("$prefix${conf.getString("remove")}", MessagesType.CHAT, sender); return
            } else {
                MessageUtil.sendMessage("$prefix${conf.getString("not-found")}", MessagesType.CHAT, sender)
            }
        }
         */
    }

    /*
    override fun tabComplete(sender: CommandSender): MutableMap<Int, List<String>> {
        val map: MutableMap<Int, List<String>> = HashMap()
        map[1] = listOf("toggle", "add", "remove")
        map[2] = listOf("<Address>")
        return map
    }
     */
    override fun tabComplete(sender: CommandSender, args: Array<out String>): MutableCollection<String>? {
        return when (args.size) {
            2 -> mutableListOf("toggle", "add", "remove")
            3 -> {
                val a = args[1]
                if (a.equals("add", ignoreCase = true) || a.equals("remove", ignoreCase = true)) mutableListOf("<Address>") else mutableListOf()
            }
            else -> null
        }
    }
}