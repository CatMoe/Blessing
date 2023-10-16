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
import catmoe.fallencrystal.translation.command.TranslationCommand
import catmoe.fallencrystal.translation.command.annotation.MoeCommand
import catmoe.fallencrystal.translation.command.annotation.misc.DescriptionType
import catmoe.fallencrystal.translation.command.bungee.BungeeCommandAdapter
import catmoe.fallencrystal.translation.executor.CommandExecutor
import catmoe.fallencrystal.translation.executor.bungee.BungeeConsole
import catmoe.fallencrystal.translation.platform.Platform
import catmoe.fallencrystal.translation.platform.ProxyPlatform
import catmoe.fallencrystal.translation.utils.config.LocalConfig
import net.md_5.bungee.api.CommandSender
import java.util.concurrent.CompletableFuture
@MoeCommand(
    name = "moefilter",
    aliases = ["mf", "moefilter"],
    permission = "",
    allowConsole = true,
    asyncExecute = true,
    descType = DescriptionType.MESSAGE_CONFIG,
    descValue = "prefix"
)
class CommandHandler : TranslationCommand {

    init {
        instance=this
    }

    private val config = LocalConfig.getMessage() // Message Config
    private val prefix: String = config.getString("prefix")
    private val fullHideCommand = config.getBoolean("command.full-hide-command")

    @Platform(ProxyPlatform.BUNGEE)
    override fun execute(sender: CommandExecutor, input: Array<out String>) {
        val casted = BungeeCommandAdapter.getCastedSender(sender) ?: return
        if (input.isEmpty() || !sender.hasPermission("moefilter")) { infoCommand(casted); return }
        val command = CommandManager.getICommand(input[0])
        if (command != null) {
            val parsedInfo = getParsedCommand(command)!!
            val permission = parsedInfo.permission
            if (sender is BungeeConsole && !parsedInfo.allowConsole) { MessageUtil.sendMessage("$prefix${config.getString("command.only-player")}", MessagesType.CHAT, casted); return }
            if (!sender.hasPermission(permission)) {
                if (fullHideCommand) { MessageUtil.sendMessage("$prefix${config.getString("command.not-found")}", MessagesType.CHAT, casted) }
                else { MessageUtil.sendMessage("$prefix${config.getString("command.no-permission").replace("[permission]", permission)}", MessagesType.CHAT, casted) }; return
            } else { execute(command, casted, input) }
        } else { MessageUtil.sendMessage("$prefix${config.getString("command.not-found")}", MessagesType.CHAT, casted) }
    }

    fun execute(command: ICommand, executor: CommandSender, args: Array<out String>) {
        val clazz = command::class.java
        if (clazz.isAnnotationPresent(MoeCommand::class.java) && clazz.getAnnotation(MoeCommand::class.java).asyncExecute) {
            CompletableFuture.runAsync { command.execute(executor, args) }
            return
        }
        command.execute(executor, args)
    }

    /*
    override fun onTabComplete(sender: CommandSender, args: Array<out String>): List<String> {
        val noPermission = if (config.getString("command.tabComplete.no-permission").isNotEmpty()) { listOf(config.getString("command.tabComplete.no-permission")) } else listOf()
        val noSubPermission = if (config.getString("command.tabComplete.no-subcommand-permission").isNotEmpty()) listOf(config.getString("command.tabComplete.no-subcommand-permission").replace("[permission]", permission)) else listOf()
        if (!sender.hasPermission("moefilter")) return noPermission
        if (args.size == 1) {
            val list = mutableListOf<String>()
            //getCommandList(sender).forEach { list.add(getParsedCommand(it)!!.command) }
            val arg0 = args[0]
            for (it in getCommandList(sender)) {
                val info = getParsedCommand(it) ?: continue
                if (arg0.isNotEmpty() && !info.command.startsWith(arg0)) continue
                list.add(info.command)
            }
            return list
        }
        val command = CommandManager.getICommand(args[0])
        return if (command != null) {
            val permission = getParsedCommand(command)!!.permission
            if (!sender.hasPermission(permission)) { if (!fullHideCommand) { noSubPermission } else { listOf() } } else command.tabComplete(sender, args).toMutableList()
        } else { listOf() }
    }
     */

    override fun tabComplete(sender: CommandExecutor, input: Array<out String>): MutableList<String> {
        val casted = BungeeCommandAdapter.getCastedSender(sender) ?: return mutableListOf()
        val noPermission = config.getString("command.tabComplete.no-permission")
        val noSubPermission = config.getString("command.tabComplete.no-subcommand-permission")
        if (!sender.hasPermission("moefilter")) return if (noPermission.isEmpty()) mutableListOf() else mutableListOf(
            noPermission
        )
        if (input.size == 1) {
            val list = mutableListOf<String>()
            for (it in getCommandList(casted)) {
                val info = getParsedCommand(it) ?: continue
                if (input[0].isNotEmpty() && !info.command.startsWith(input[0])) continue
                list.add(info.command)
            }
            return list
        }
        val command = CommandManager.getICommand(input[0]) ?: return mutableListOf()
        if (!sender.hasPermission(getParsedCommand(command)!!.command)) {
            return if (fullHideCommand || noSubPermission.isEmpty()) mutableListOf() else mutableListOf(noSubPermission)
        }
        return command.tabComplete(casted, input)?.toMutableList() ?: mutableListOf()
    }

    private fun infoCommand(sender: CommandSender) {
        MessageUtil.sendMessage(
            listOf(
                "",
                "  <aqua>Moe<white>Filter <gray>- <white>${MoeFilterBungee.instance.description.version}",
                "",
                if (sender.hasPermission("moefilter")) "  <yellow>使用 <white>/moefilter help <yellow>查看命令列表" else " <white> github.com/CatMoe/MoeFilter",
                ""
            ).joinToString("<reset><newline>"),
            MessagesType.CHAT, sender
        )
    }

    companion object {
        lateinit var instance: CommandHandler
            private set
    }

}