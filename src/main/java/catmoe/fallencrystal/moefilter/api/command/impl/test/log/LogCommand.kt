package catmoe.fallencrystal.moefilter.api.command.impl.test.log

import catmoe.fallencrystal.moefilter.api.command.ICommand
import net.md_5.bungee.api.CommandSender

class LogCommand : ICommand {
    override fun command(): String { return "logger" }

    override fun allowedConsole(): Boolean { return true }

    override fun description(): String { return "将控制台的消息打印到聊天中" }

    override fun permission(): String { return "moefilter.logger" }

    override fun execute(sender: CommandSender, args: Array<out String>?) {
        if (LogBroadcast.isInList(sender)) {
            LogBroadcast.removePlayer(sender)
        } else {
            LogBroadcast.addPlayer(sender)
        }
    }

    override fun tabComplete(): MutableMap<Int, List<String>> { return mutableMapOf() }
}