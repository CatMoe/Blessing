package catmoe.fallencrystal.moefilter.api.command.impl

import catmoe.fallencrystal.moefilter.api.command.ICommand
import catmoe.fallencrystal.moefilter.api.command.OCommand
import catmoe.fallencrystal.moefilter.common.config.ObjectConfig
import catmoe.fallencrystal.moefilter.util.message.MessageUtil
import net.md_5.bungee.api.CommandSender

class HelpCommand : ICommand {
    override fun command(): String { return "help" }

    override fun allowedConsole(): Boolean { return true }

    override fun description(): String { return ObjectConfig.getMessage().getString("command.description.help") }

    override fun usage(): List<String> { return listOf("/moefilter help", "/moefilter help <command>") }

    override fun permission(): String { return "moefilter.help" }

    private val config = ObjectConfig.getMessage()
    private val prefix: String = config.getString("prefix")

    override fun execute(sender: CommandSender, args: Array<out String>?) {
        val line = "&b&m&l                                                            "
        if (args!!.size < 2) {
            sendMessage(sender,line)
            OCommand.getCommandList(sender).forEach { sendMessage(sender, "  &f/moefilter ${it.command()} &b- &f ${it.description()}") }
            sendMessage(sender,line)
            return
        } else {
            try {
                val subCommand = args[1].let { OCommand.getICommand(it) }
                if (!sender.hasPermission(subCommand!!.permission())) { MessageUtil.sendMessage(sender, "$prefix${config.getString("command.not-found")}"); return }
                val description = subCommand.description()
                val command = subCommand.command()
                // sendMessage(sender, "  &f/moefilter $command &b- &f$description")
                val commandUsage = subCommand.usage()
                val message = listOf(
                    "",
                    "  &b命令: &f$command",
                    "  &b描述: &f$description",
                    "",
                    "  &e此命令一共有${commandUsage.size} 个用法"
                )
                message.forEach { MessageUtil.sendMessage(sender, it) }
                if (commandUsage.isNotEmpty()) { commandUsage.forEach { MessageUtil.sendMessage(sender, "  &e$it") } }
                MessageUtil.sendMessage(sender, "")
            } catch (_: ArrayIndexOutOfBoundsException) {
            } catch (_: NullPointerException) {
                val message = config.getString("command.not-found")
                MessageUtil.sendMessage(sender, "$prefix$message")
            }
        }
    }

    override fun tabComplete(sender: CommandSender): MutableMap<Int, List<String>> {
        val map: MutableMap<Int, List<String>> = HashMap()
        val list = mutableListOf<String>()
        OCommand.getCommandList(sender).forEach { if (sender.hasPermission(it.permission())) { list.add(it.command()) } }
        map[1] = list
        return map
    }

    private fun sendMessage(sender: CommandSender, message: String) { MessageUtil.sendMessage(sender, message) }
}