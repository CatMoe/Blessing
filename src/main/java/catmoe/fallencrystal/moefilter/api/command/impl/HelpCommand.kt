package catmoe.fallencrystal.moefilter.api.command.impl

import catmoe.fallencrystal.moefilter.api.command.CommandManager
import catmoe.fallencrystal.moefilter.api.command.ICommand
import catmoe.fallencrystal.moefilter.api.command.annotation.*
import catmoe.fallencrystal.moefilter.api.command.annotation.misc.DescriptionFrom
import catmoe.fallencrystal.moefilter.common.config.LocalConfig
import catmoe.fallencrystal.moefilter.util.message.v2.MessageUtil
import catmoe.fallencrystal.moefilter.util.message.v2.packet.type.MessagesType
import net.md_5.bungee.api.CommandSender

@Command("help")
@CommandDescription(DescriptionFrom.MESSAGE_PATH, "command.description.help")
@CommandPermission("moefilter.help")
@CommandUsage(["/moefilter help", "/moefilter help <command>"])
@ConsoleCanExecute
class HelpCommand : ICommand {

    private val config = LocalConfig.getMessage()
    private val prefix: String = config.getString("prefix")

    override fun execute(sender: CommandSender, args: Array<out String>?) {
        val line = ""
        if (args!!.size < 2) {
            MessageUtil.sendMessage(line, MessagesType.CHAT, sender)
            // OCommand.getCommandList(sender).forEach { sendMessage(sender, "  &f/moefilter ${it.command()} &b- &f ${it.description()}") }
            for (it in CommandManager.getCommandList(sender)) {
                val parsedInfo = CommandManager.getParsedCommand(it)
                if (parsedInfo == null) { MessageUtil.sendMessage("$prefix${config.getString("command.not-found")}", MessagesType.CHAT, sender); return }
                MessageUtil.sendMessage("  <white>/moefilter ${parsedInfo.command} <aqua>- <reset>${parsedInfo.description}", MessagesType.CHAT, sender)
            }
            MessageUtil.sendMessage(line, MessagesType.CHAT, sender)
            return
        } else {
            try {
                val subCommand = CommandManager.getICommand(args[1])
                if (subCommand == null) { MessageUtil.sendMessage("$prefix${config.getString("command.not-found")}", MessagesType.CHAT, sender); return }
                val parsedInfo = CommandManager.getParsedCommand(subCommand)!!
                if (!sender.hasPermission(parsedInfo.permission)) { MessageUtil.sendMessage("$prefix${config.getString("command.not-found")}", MessagesType.CHAT, sender); return }
                val description = parsedInfo.description
                val command = parsedInfo.command
                // sendMessage(sender, "  &f/moefilter $command &b- &f$description")
                val commandUsage = parsedInfo.usage
                val message = listOf(
                    "",
                    "  <aqua>命令: &f$command",
                    "  <aqua>描述: &f$description",
                    "",
                    "  <yellow>此命令一共有${commandUsage.size} 个用法"
                )
                message.forEach { MessageUtil.sendMessage(it, MessagesType.CHAT, sender) }
                if (commandUsage.isNotEmpty()) { commandUsage.forEach { MessageUtil.sendMessage("  <yellow>$it", MessagesType.CHAT, sender) } }
                MessageUtil.sendMessage("", MessagesType.CHAT, sender)
            } catch (_: ArrayIndexOutOfBoundsException) {
            } catch (e: NullPointerException) {
                val message = config.getString("command.not-found")
                MessageUtil.sendMessage("$prefix$message", MessagesType.CHAT, sender)
            }
        }
    }

    override fun tabComplete(sender: CommandSender): MutableMap<Int, List<String>> {
        val map: MutableMap<Int, List<String>> = HashMap()
        val list = mutableListOf<String>()
        CommandManager.getCommandList(sender).forEach {
            val parsedInfo = CommandManager.getParsedCommand(it)!!
            if (sender.hasPermission(parsedInfo.permission)) { list.add(parsedInfo.command) }
        }
        map[1] = list
        return map
    }
}