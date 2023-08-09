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
import catmoe.fallencrystal.moefilter.common.config.LocalConfig
import catmoe.fallencrystal.moefilter.common.firewall.lockdown.LockdownManager
import catmoe.fallencrystal.moefilter.util.message.v2.MessageUtil
import catmoe.fallencrystal.moefilter.util.message.v2.packet.type.MessagesType
import net.md_5.bungee.api.CommandSender
import java.net.InetAddress

@CommandDescription(DescriptionFrom.STRING, "Lockdown your server")
@CommandUsage([
    "/moefilter lockdown toggle",
    "/moefilter lockdown add \"<Address>\"",
    "/moefilter lockdown remove \"<Address>\""
])
@ConsoleCanExecute
class LockdownCommand : ICommand {

    override fun execute(sender: CommandSender, args: Array<out String>) {
        if (args.size < 2 || args.size > 3) {
            listOf(
                "",
                " <aqua>Lockdown状态 <white>: ${if (LockdownManager.state.get()) "<green>启用" else "<red>禁用"}",
                "",
                " lockdown toggle <aqua>-<white> 开启/关闭锁定模式",
                " lockdown add <Address> <aqua>-<white> 允许指定地址可以在锁定状态下加入",
                " lockdown remove <Address> <aqua>-<white> 禁止指定地址可以在锁定状态下加入",
                "",
            ).forEach { MessageUtil.sendMessage(it, MessagesType.CHAT, sender) }
            return
        }
        val args2 = args[1]
        val prefix = LocalConfig.getMessage().getString("prefix")
        if (args2.equals("toggle", ignoreCase = true)) {
            when (LockdownManager.state.get()) {
                true -> {
                    LockdownManager.setLockdown(false)
                    MessageUtil.sendMessage("$prefix<green>已关闭锁定模式", MessagesType.CHAT, sender)
                }
                false -> {
                    LockdownManager.setLockdown(true)
                    MessageUtil.sendMessage("$prefix<red>已开启锁定模式", MessagesType.CHAT, sender)
                }
            }
        }
        if (args2.equals("add", ignoreCase = true)) {
            val address: InetAddress? = try { InetAddress.getByName(args[2]) } catch (_: Exception) { null }
            if (address == null) { MessageUtil.sendMessage("$prefix<red>请键入一个有效的地址", MessagesType.CHAT, sender); return }
            if (LockdownManager.whitelistCache.getIfPresent(address) != null) {
                MessageUtil.sendMessage("$prefix<red>此地址已在允许连接的列表中了!", MessagesType.CHAT, sender); return
            } else {
                LockdownManager.whitelistCache.put(address, true)
                MessageUtil.sendMessage("$prefix<green>已将此地址加入允许连接的列表中", MessagesType.CHAT, sender)
            }
        }
        if (args2.equals("remove", ignoreCase = true)) {
            val address: InetAddress? = try { InetAddress.getByName(args[2]) } catch (_: Exception) { null }
            if (address == null) { MessageUtil.sendMessage("$prefix<red>请键入一个有效的地址", MessagesType.CHAT, sender); return }
            if (LockdownManager.whitelistCache.getIfPresent(address) != null) {
                LockdownManager.whitelistCache.invalidate(address)
                MessageUtil.sendMessage("$prefix<green>已将此地址从允许连接的列表中删除", MessagesType.CHAT, sender); return
            } else {
                MessageUtil.sendMessage("$prefix<red>允许连接的列表中没有该地址!", MessagesType.CHAT, sender)
            }
        }
    }

    override fun tabComplete(sender: CommandSender): MutableMap<Int, List<String>> {
        val map: MutableMap<Int, List<String>> = HashMap()
        map[1] = listOf("toggle", "add", "remove")
        map[2] = listOf("<Address>")
        return map
    }
}