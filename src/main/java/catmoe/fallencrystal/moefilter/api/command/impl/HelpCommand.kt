package catmoe.fallencrystal.moefilter.api.command.impl

import catmoe.fallencrystal.moefilter.api.command.ICommand
import catmoe.fallencrystal.moefilter.api.command.OCommand
import catmoe.fallencrystal.moefilter.util.MessageUtil
import net.md_5.bungee.api.CommandSender

class HelpCommand : ICommand {
    override fun command(): String { return "help" }

    override fun allowedConsole(): Boolean { return true }

    override fun description(): String { return "列出所有已注册的命令" }

    override fun permission(): String { return "moefilter.list" }

    override fun execute(sender: CommandSender, args: Array<out String>?) {
        val subCommand = args?.get(1)?.let { OCommand.getICommand(it) }
        if (subCommand != null) {
            val description = subCommand.description()
            val command = subCommand.command()
            sendMessage(sender, "  &f/moefilter $command &b- &f$description")
            return
        }
        val line = "&b&m&l                                                            "
        sendMessage(sender,line)
        OCommand.iCommandList().forEach { sendMessage(sender, "  &f/moefilter ${it.command()} &b- &f ${it.description()}") }
        sendMessage(sender,line)
    }

    override fun tabComplete(): MutableMap<Int, List<String>> {
        val map: MutableMap<Int, List<String>> = HashMap()
        map[1] = OCommand.commandList()
        return map
    }

    private fun sendMessage(sender: CommandSender, message: String) { MessageUtil.sendMessage(sender, message) }
}