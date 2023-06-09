package catmoe.fallencrystal.moefilter.api.command

import net.md_5.bungee.api.CommandSender

interface ICommand {

    fun execute(sender: CommandSender, args: Array<out String>?)

    fun tabComplete(sender: CommandSender): MutableMap<Int, List<String>>
}