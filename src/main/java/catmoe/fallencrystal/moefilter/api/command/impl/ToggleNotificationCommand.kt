package catmoe.fallencrystal.moefilter.api.command.impl

import catmoe.fallencrystal.moefilter.api.command.ICommand
import catmoe.fallencrystal.moefilter.api.command.annotation.Command
import catmoe.fallencrystal.moefilter.api.command.annotation.CommandDescription
import catmoe.fallencrystal.moefilter.api.command.annotation.CommandPermission
import catmoe.fallencrystal.moefilter.api.command.annotation.CommandUsage
import catmoe.fallencrystal.moefilter.api.command.annotation.misc.DescriptionFrom
import catmoe.fallencrystal.moefilter.common.config.LocalConfig
import catmoe.fallencrystal.moefilter.util.message.MessageUtil
import catmoe.fallencrystal.moefilter.util.message.notification.Notifications
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.connection.ProxiedPlayer

@Command("actionbar")
@CommandDescription(DescriptionFrom.MESSAGE_PATH, "actionbar.command.description")
@CommandUsage(["/moefilter actionbar"])
@CommandPermission("moefilter.notification")
// ConsoleCanExecute is not here. so only can execute this command by who online player has permission.
class ToggleNotificationCommand : ICommand {
    private val config = LocalConfig.getMessage()
    private val prefix = config.getString("prefix")
    private val enable = config.getString("actionbar.command.enable")
    private val disable = config.getString("actionbar.command.disable")

    override fun execute(sender: CommandSender, args: Array<out String>?) {
        if (Notifications.toggleSpyNotificationPlayer(sender as ProxiedPlayer)) { MessageUtil.sendMessage(sender, MessageUtil.colorizeMiniMessage("$prefix$enable"))
        } else { MessageUtil.sendMessage(sender, MessageUtil.colorizeMiniMessage("$prefix$disable")) }
    }

    override fun tabComplete(sender: CommandSender): MutableMap<Int, List<String>> { return mutableMapOf() }
}