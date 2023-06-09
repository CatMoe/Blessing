package catmoe.fallencrystal.moefilter.api.command.impl.test.log

import catmoe.fallencrystal.moefilter.api.command.ICommand
import catmoe.fallencrystal.moefilter.api.command.annotation.*
import catmoe.fallencrystal.moefilter.api.command.annotation.misc.DescriptionFrom
import net.md_5.bungee.api.CommandSender

@Command("logger")
@CommandDescription(DescriptionFrom.STRING, "将控制台的消息打印到聊天中")
@CommandUsage(["/moefilter logger"])
@CommandPermission("moefilter.logger")
@DebugCommand
class LogCommand : ICommand {

    override fun execute(sender: CommandSender, args: Array<out String>?) { if (LogBroadcast.isInList(sender)) { LogBroadcast.removePlayer(sender) } else { LogBroadcast.addPlayer(sender) } }

    override fun tabComplete(sender: CommandSender): MutableMap<Int, List<String>> { return mutableMapOf() }
}