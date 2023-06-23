package catmoe.fallencrystal.moefilter.util.plugin

import catmoe.fallencrystal.moefilter.api.command.CommandManager
import catmoe.fallencrystal.moefilter.api.command.impl.HelpCommand
import catmoe.fallencrystal.moefilter.api.command.impl.ReloadCommand
import catmoe.fallencrystal.moefilter.api.command.impl.ToggleNotificationCommand
import catmoe.fallencrystal.moefilter.api.command.impl.test.TestKickCommand
import catmoe.fallencrystal.moefilter.api.command.impl.test.event.MessageEvent
import catmoe.fallencrystal.moefilter.api.command.impl.test.event.TestEventCommand
import catmoe.fallencrystal.moefilter.api.command.impl.test.log.LogCommand
import catmoe.fallencrystal.moefilter.api.command.impl.test.log.LogSpyUnload
import catmoe.fallencrystal.moefilter.api.event.EventManager

class LoadCommand {

    fun load(){ loadCommand() }

    fun reload() { CommandManager.dropAll(); loadCommand() }

    private fun loadCommand() {
        CommandManager.register(HelpCommand())
        CommandManager.register(ToggleNotificationCommand())

        // All debug command managers by @DebugCommand annotation
        EventManager.registerListener(FilterPlugin.getPlugin()!!, MessageEvent())
        EventManager.registerListener(FilterPlugin.getPlugin()!!, LogSpyUnload())
        CommandManager.register(LogCommand())
        CommandManager.register(TestEventCommand())
        CommandManager.register(ReloadCommand())
        CommandManager.register(TestKickCommand())
    }
}