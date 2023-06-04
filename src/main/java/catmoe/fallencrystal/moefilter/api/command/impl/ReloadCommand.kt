package catmoe.fallencrystal.moefilter.api.command.impl

import catmoe.fallencrystal.moefilter.api.command.ICommand
import catmoe.fallencrystal.moefilter.api.event.EventManager
import catmoe.fallencrystal.moefilter.api.event.events.PluginReloadEvent
import catmoe.fallencrystal.moefilter.common.config.ObjectConfig
import net.md_5.bungee.api.CommandSender

class ReloadCommand : ICommand {
    override fun command(): String { return "reload" }

    override fun allowedConsole(): Boolean { return true }

    override fun description(): String { return ObjectConfig.getMessage().getString("command.description.reload") }

    override fun usage(): List<String> { return listOf("/moefilter reload") }

    override fun permission(): String { return "moefilter.reload" }

    override fun execute(sender: CommandSender, args: Array<out String>?) { EventManager.triggerEvent(PluginReloadEvent(sender)) }

    override fun tabComplete(sender: CommandSender): MutableMap<Int, List<String>> { return mutableMapOf() }
}