package catmoe.fallencrystal.moefilter.util.plugin

import catmoe.fallencrystal.moefilter.api.command.CommandManager
import catmoe.fallencrystal.moefilter.api.command.impl.HelpCommand
import catmoe.fallencrystal.moefilter.api.command.impl.ReloadCommand
import catmoe.fallencrystal.moefilter.api.command.impl.ToggleNotificationCommand
import catmoe.fallencrystal.moefilter.api.command.impl.test.TestKickCommand
import catmoe.fallencrystal.moefilter.api.command.impl.test.gui.TestGuiCommand

class LoadCommand {

    fun load(){ loadCommand() }

    fun reload() { CommandManager.dropAll(); loadCommand() }

    private fun loadCommand() {
        CommandManager.register(HelpCommand())
        CommandManager.register(ToggleNotificationCommand())

        // All debug command managers by @DebugCommand annotation
        CommandManager.register(ReloadCommand())
        CommandManager.register(TestKickCommand())
        CommandManager.register(TestGuiCommand())
    }
}