package catmoe.fallencrystal.moefilter.api.command.impl.test

import catmoe.fallencrystal.moefilter.api.command.ICommand
import catmoe.fallencrystal.moefilter.api.command.annotation.*
import catmoe.fallencrystal.moefilter.api.command.annotation.misc.DescriptionFrom
import catmoe.fallencrystal.moefilter.network.bungee.util.bconnection.ConnectionUtil
import catmoe.fallencrystal.moefilter.network.bungee.util.kick.DisconnectType
import catmoe.fallencrystal.moefilter.network.bungee.util.kick.FastDisconnect
import net.md_5.bungee.UserConnection
import net.md_5.bungee.api.CommandSender

@DebugCommand
@Command("testkick")
@CommandPermission("moefilter.testkick")
@CommandUsage(["/moefilter testkick"])
@CommandDescription(DescriptionFrom.STRING, "testkick")
class TestKickCommand : ICommand {
    override fun execute(sender: CommandSender, args: Array<out String>?) {
        val connection = ConnectionUtil((sender as UserConnection).pendingConnection)
        FastDisconnect.disconnect(connection, DisconnectType.ALREADY_ONLINE)
    }

    override fun tabComplete(sender: CommandSender): MutableMap<Int, List<String>> { return mutableMapOf() }
}