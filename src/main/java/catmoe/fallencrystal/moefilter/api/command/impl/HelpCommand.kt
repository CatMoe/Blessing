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

    override fun permission(): String { return "moefilter.list" }

    val config = ObjectConfig.getMessage()
    val prefix: String = config.getString("prefix")

    override fun execute(sender: CommandSender, args: Array<out String>?) {
        val line = "&b&m&l                                                            "
        if (args!!.size < 2) {
            sendMessage(sender,line)
            OCommand.iCommandList().forEach { sendMessage(sender, "  &f/moefilter ${it.command()} &b- &f ${it.description()}") }
            sendMessage(sender,line)
            return
        } else {
            try {
                val subCommand = args[1].let { OCommand.getICommand(it) }
                val description = subCommand!!.description()
                val command = subCommand.command()
                sendMessage(sender, "  &f/moefilter $command &b- &f$description")
            } catch (_: ArrayIndexOutOfBoundsException) {
            } catch (_: NullPointerException) {
                val message = config.getString("command.not-found")
                MessageUtil.sendMessage(sender, "$prefix$message")
            }
        }
    }

    override fun tabComplete(): MutableMap<Int, List<String>> {
        val map: MutableMap<Int, List<String>> = HashMap()
        map[1] = OCommand.commandList()
        return map
    }

    private fun sendMessage(sender: CommandSender, message: String) { MessageUtil.sendMessage(sender, message) }
}