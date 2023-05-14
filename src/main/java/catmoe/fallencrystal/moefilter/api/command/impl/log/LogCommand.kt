package catmoe.fallencrystal.moefilter.api.command.impl.log

import catmoe.fallencrystal.moefilter.api.command.ICommand
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.connection.ProxiedPlayer

class LogCommand : ICommand {
    override fun command(): String { return "logger" }

    override fun allowedConsole(): Boolean { return false }

    override fun description(): String { return "将控制台的消息打印到聊天中" }

    override fun permission(): String { return "moefilter.logger" }

    override fun execute(sender: CommandSender, args: Array<out String>?) {
        val player = sender as ProxiedPlayer
        if (LogBroadcast.isInList(player)) {
            LogBroadcast.removePlayer(player)
        } else {
            LogBroadcast.addPlayer(player)
        }
    }

    override fun tabComplete(): MutableMap<Int, List<String>> { return mutableMapOf() }
}