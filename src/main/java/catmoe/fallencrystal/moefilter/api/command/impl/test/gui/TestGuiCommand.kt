package catmoe.fallencrystal.moefilter.api.command.impl.test.gui

import catmoe.fallencrystal.moefilter.api.command.ICommand
import catmoe.fallencrystal.moefilter.api.command.annotation.CommandDescription
import catmoe.fallencrystal.moefilter.api.command.annotation.CommandUsage
import catmoe.fallencrystal.moefilter.api.command.annotation.misc.DescriptionFrom
import dev.simplix.protocolize.data.inventory.InventoryType
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.connection.ProxiedPlayer

@CommandDescription(DescriptionFrom.STRING, "open a test gui")
@CommandUsage(["/moefilter gui"])
class TestGuiCommand : ICommand {
    override fun execute(sender: CommandSender, args: Array<out String>?) {
        val menu = TestGui()
        menu.setPlayer(sender as ProxiedPlayer)
        menu.type(InventoryType.GENERIC_9X3)
        menu.setTitle(menu.colorize("<gradient:green:yellow>a test gui</gradient>"))
        menu.open()
    }

    override fun tabComplete(sender: CommandSender): MutableMap<Int, List<String>> { return mutableMapOf() }
}