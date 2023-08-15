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
import catmoe.fallencrystal.moefilter.api.command.annotation.CommandDescription
import catmoe.fallencrystal.moefilter.api.command.annotation.CommandUsage
import catmoe.fallencrystal.moefilter.api.command.annotation.ConsoleCanExecute
import catmoe.fallencrystal.moefilter.api.command.annotation.misc.DescriptionFrom
import catmoe.fallencrystal.translation.utils.config.LocalConfig
import catmoe.fallencrystal.moefilter.common.firewall.lockdown.LockdownManager
import catmoe.fallencrystal.moefilter.util.message.v2.MessageUtil
import catmoe.fallencrystal.moefilter.util.message.v2.packet.type.MessagesType
import net.md_5.bungee.api.CommandSender
import java.net.InetAddress

@CommandDescription(DescriptionFrom.MESSAGE_PATH, "lockdown.description")
@CommandUsage([
    "/moefilter lockdown toggle",
    "/moefilter lockdown add \"<Address>\"",
    "/moefilter lockdown remove \"<Address>\""
])
@ConsoleCanExecute
class LockdownCommand : ICommand {

    override fun execute(sender: CommandSender, args: Array<out String>) {
        val conf = LocalConfig.getMessage().getConfig("lockdown")
        if (args.size < 2 || args.size > 3) {
            val state = if (LockdownManager.state.get()) conf.getString("enabled") else conf.getString("disabled")
            val stateMessage = conf.getStringList("state-message").joinToString("\n").replace("[state]", state)
            MessageUtil.sendMessage(stateMessage, MessagesType.CHAT, sender)
            return
        }
        val args2 = args[1]
        val prefix = LocalConfig.getMessage().getString("prefix")
        when (args2) {
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

    override fun tabComplete(sender: CommandSender): MutableMap<Int, List<String>> {
        val map: MutableMap<Int, List<String>> = HashMap()
        map[1] = listOf("toggle", "add", "remove")
        map[2] = listOf("<Address>")
        return map
    }
}