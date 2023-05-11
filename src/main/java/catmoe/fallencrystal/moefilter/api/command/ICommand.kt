package catmoe.fallencrystal.moefilter.api.command

import net.md_5.bungee.api.CommandSender

interface ICommand {
    fun command(): String

    fun allowedConsole(): Boolean

    fun description(): String

    fun permission(): String

    fun execute(sender: CommandSender, args: Array<out String>?)

    fun tabComplete(): MutableMap<Int, List<String>>
}