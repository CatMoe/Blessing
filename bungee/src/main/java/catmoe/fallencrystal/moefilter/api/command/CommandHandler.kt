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

package catmoe.fallencrystal.moefilter.api.command

import catmoe.fallencrystal.moefilter.MoeFilterBungee
import catmoe.fallencrystal.moefilter.api.command.CommandManager.getCommandList
import catmoe.fallencrystal.moefilter.api.command.CommandManager.getParsedCommand
import catmoe.fallencrystal.moefilter.util.message.v2.MessageUtil
import catmoe.fallencrystal.moefilter.util.message.v2.packet.type.MessagesType
import catmoe.fallencrystal.translation.command.annotation.MoeCommand
import catmoe.fallencrystal.translation.utils.config.LocalConfig
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.plugin.TabExecutor
import java.util.concurrent.CompletableFuture

class CommandHandler(name: String?, permission: String?, vararg aliases: String?) : net.md_5.bungee.api.plugin.Command(name, permission, *aliases), TabExecutor {

    private val config = LocalConfig.getMessage() // Message Config
    private val prefix: String = config.getString("prefix")
    private val fullHideCommand = config.getBoolean("command.full-hide-command")

    override fun execute(sender: CommandSender, args: Array<out String>) {
        // 当玩家没有权限或未输入任何子命令时  详见 infoCommand 方法.
        if (args.isEmpty() || !sender.hasPermission("moefilter")) { infoCommand(sender); return }
        val command = CommandManager.getICommand(args[0])
        if (command != null) {
            val parsedInfo = getParsedCommand(command)!!
            val permission = parsedInfo.permission
            if (sender !is ProxiedPlayer && !parsedInfo.allowConsole) { MessageUtil.sendMessage("$prefix${config.getString("command.only-player")}", MessagesType.CHAT, sender); return }
            if (!sender.hasPermission(permission)) {
                if (fullHideCommand) { MessageUtil.sendMessage("$prefix${config.getString("command.not-found")}", MessagesType.CHAT, sender) }
                else { MessageUtil.sendMessage("$prefix${config.getString("command.no-permission").replace("[permission]", permission)}", MessagesType.CHAT, sender) }; return
            }
            else { execute(command, sender, args) }
        } else { MessageUtil.sendMessage("$prefix${config.getString("command.not-found")}", MessagesType.CHAT, sender) }
    }

    @Suppress("DEPRECATION")
    fun execute(command: ICommand, executor: CommandSender, args: Array<out String>) {
        val clazz = command::class.java
        if (clazz.isAnnotationPresent(catmoe.fallencrystal.translation.command.annotation.AsyncExecute::class.java) || (clazz.isAnnotationPresent(MoeCommand::class.java) && clazz.getAnnotation(MoeCommand::class.java).asyncExecute)) {
            CompletableFuture.runAsync { command.execute(executor, args) }
            return
        }
        command.execute(executor, args)
    }

    override fun onTabComplete(sender: CommandSender, args: Array<out String>): List<String> {
        val noPermission = if (config.getString("command.tabComplete.no-permission").isNotEmpty()) { listOf(config.getString("command.tabComplete.no-permission")) } else listOf()
        val noSubPermission = if (config.getString("command.tabComplete.no-subcommand-permission").isNotEmpty()) listOf(config.getString("command.tabComplete.no-subcommand-permission").replace("[permission]", permission)) else listOf()
        if (!sender.hasPermission("moefilter")) return noPermission
        if (args.size == 1) { val list = mutableListOf<String>(); getCommandList(sender).forEach { list.add(getParsedCommand(it)!!.command) }; return list }
        val command = CommandManager.getICommand(args[0])
        return if (command != null) {
            val permission = getParsedCommand(command)!!.permission
            if (!sender.hasPermission(permission)) { if (!fullHideCommand) { noSubPermission } else { listOf() } } else command.tabComplete(sender)[args.size - 1] ?: listOf()
        } else { listOf() }
    }

    private fun infoCommand(sender: CommandSender) {
        val version = MoeFilterBungee.instance.description.version
        val line = if (sender.hasPermission("moefilter")) "  <yellow>使用 <white>/moefilter help <yellow>查看命令列表" else " <white> github.com/CatMoe/MoeFilter"
        MessageUtil.sendMessage(
            listOf(
                "",
                "  <aqua>Moe<white>Filter <gray>- <white>$version",
                "",
                line,
                ""
            ).joinToString("<reset><newline>"),
            MessagesType.CHAT, sender
        )
    }

}