package catmoe.fallencrystal.moefilter.api.command.impl

import catmoe.fallencrystal.moefilter.api.command.ICommand
import catmoe.fallencrystal.moefilter.api.command.annotation.*
import catmoe.fallencrystal.moefilter.api.command.annotation.misc.DescriptionFrom
import catmoe.fallencrystal.moefilter.api.event.EventManager
import catmoe.fallencrystal.moefilter.api.event.events.PluginReloadEvent
import net.md_5.bungee.api.CommandSender

@Command("reload")
@ConsoleCanExecute
@CommandDescription(DescriptionFrom.MESSAGE_PATH, "command.description.reload")
@CommandUsage(["/moefilter reload"])
@CommandPermission("moefilter.reload")
class ReloadCommand : ICommand {

    override fun execute(sender: CommandSender, args: Array<out String>?) { EventManager.triggerEvent(PluginReloadEvent(sender)) }

    override fun tabComplete(sender: CommandSender): MutableMap<Int, List<String>> { return mutableMapOf() }
}