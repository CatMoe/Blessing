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

import catmoe.fallencrystal.moefilter.api.command.CommandManager
import catmoe.fallencrystal.moefilter.api.command.ICommand
import catmoe.fallencrystal.moefilter.util.message.v2.MessageUtil
import catmoe.fallencrystal.moefilter.util.message.v2.packet.type.MessagesType
import catmoe.fallencrystal.translation.command.annotation.MoeCommand
import catmoe.fallencrystal.translation.command.annotation.misc.DescriptionType
import catmoe.fallencrystal.translation.utils.config.LocalConfig
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.connection.ProxiedPlayer

/*
@Command("help")
@CommandDescription(DescriptionFrom.MESSAGE_PATH, "command.description.help")
@CommandPermission("moefilter.help")
@CommandUsage(["/moefilter help", "/moefilter help <command>"])
@ConsoleCanExecute
 */
@MoeCommand(
    name = "help",
    permission = "moefilter.help",
    descType = DescriptionType.MESSAGE_CONFIG,
    descValue = "command.description.help",
    usage = ["/moefilter help", "/moefilter help <command>"],
    allowConsole = true
)
class HelpCommand : ICommand {

    private val config get() = LocalConfig.getMessage()
    private val prefix: String get() = config.getString("prefix")
    private val notFound get() = config.getString("command.not-found")

    override fun execute(sender: CommandSender, args: Array<out String>) {
        val notFound = this.notFound
        val prefix = this.prefix
        if (args.size < 2) checkCommandList(sender, notFound, prefix) else checkCommand(sender, args, notFound, prefix)
    }

    private fun checkCommand(sender: CommandSender, args: Array<out String>, notFound: String, prefix: String) {
        try {
            val subCommand = CommandManager.getICommand(args[1])
            if (subCommand == null) { MessageUtil.sendMessage("$prefix$notFound", MessagesType.CHAT, sender); return }
            val parsedInfo = CommandManager.getParsedCommand(subCommand)!!
            if (!sender.hasPermission(parsedInfo.permission)) { MessageUtil.sendMessage("$prefix$notFound", MessagesType.CHAT, sender); return }
            val description = parsedInfo.description
            val command = parsedInfo.command
            // sendMessage(sender, "  &f/moefilter $command &b- &f$description")
            val commandUsage = parsedInfo.usage
            listOf(
                " ",
                "  <aqua>命令: <white>$command",
                "  <aqua>描述: <white>$description",
                " ",
                "  <yellow>此命令一共有${commandUsage.size} 个用法"
            ).forEach { MessageUtil.sendMessage(it, MessagesType.CHAT, sender) }
            if (commandUsage.isNotEmpty()) { commandUsage.forEach { MessageUtil.sendMessage("  <yellow>$it", MessagesType.CHAT, sender) } }
            MessageUtil.sendMessage("", MessagesType.CHAT, sender)
        } catch (_: ArrayIndexOutOfBoundsException) {
        } catch (e: NullPointerException) {
            MessageUtil.sendMessage("$prefix$notFound", MessagesType.CHAT, sender)
        }
    }

    private fun checkCommandList(sender: CommandSender, notFound: String, prefix: String) {
        val line = ""
        MessageUtil.sendMessage(line, MessagesType.CHAT, sender)
        // OCommand.getCommandList(sender).forEach { sendMessage(sender, "  &f/moefilter ${it.command()} &b- &f ${it.description()}") }
        for (it in CommandManager.getCommandList(sender)) {
            val parsedInfo = CommandManager.getParsedCommand(it)
            if (parsedInfo == null) { MessageUtil.sendMessage("$prefix$notFound", MessagesType.CHAT, sender); return }
            val description = if (sender is ProxiedPlayer) {
                val l: MutableList<String> = ArrayList()
                val u = parsedInfo.usage
                l.addAll(listOf(
                    "<hover:show_text:'",
                    "<aqua>所需权限: ${parsedInfo.permission}<reset><newline>",
                    if (u.isNotEmpty()) "<aqua>此命令一共有 <white>${u.size} <aqua>个用法: " else "<aqua>此命令没有已知的用法.",
                    "<reset><newline>"
                ))
                if (u.isNotEmpty()) parsedInfo.usage.forEach { l.add("  <white>$it<reset><newline>") }
                l.addAll(listOf(
                    if (parsedInfo.allowConsole) "<green>此命令允许控制台执行" else "<red>此命令仅允许在线玩家执行",
                    "'><click:run_command:/moefilter help ${parsedInfo.command}>",
                    "${parsedInfo.description}</hover>"
                ))
                l.joinToString("")
            } else parsedInfo.description
            // ${parsedInfo.description}
            val raw = parsedInfo.command
            val command = if (sender !is ProxiedPlayer) raw else "<click:suggest_command:/moefilter $raw>$raw</click>"
            MessageUtil.sendMessage("  <white>/moefilter $command <aqua>- <reset>$description", MessagesType.CHAT, sender)
        }
        MessageUtil.sendMessage(line, MessagesType.CHAT, sender)
        return
    }

    override fun tabComplete(sender: CommandSender, args: Array<out String>): MutableCollection<String> {
        val list = mutableListOf<String>()
        if (args.size == 2)  {
            CommandManager.getCommandList(sender).forEach {
                val parsedInfo = CommandManager.getParsedCommand(it)!!
                if (sender.hasPermission(parsedInfo.permission)) { list.add(parsedInfo.command) }
            }
        }
        return if (list.isNotEmpty()) CommandManager.sortContext(args[1], list) else list
    }
}