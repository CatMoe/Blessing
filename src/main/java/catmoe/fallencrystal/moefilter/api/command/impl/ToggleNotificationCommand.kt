package catmoe.fallencrystal.moefilter.api.command.impl

import catmoe.fallencrystal.moefilter.api.command.ICommand
import catmoe.fallencrystal.moefilter.common.config.ObjectConfig
import catmoe.fallencrystal.moefilter.util.message.MessageUtil
import catmoe.fallencrystal.moefilter.util.message.notification.Notifications
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.connection.ProxiedPlayer

class ToggleNotificationCommand : ICommand {
    private val config = ObjectConfig.getMessage()
    private val prefix = config.getString("prefix")
    private val enable = config.getString("actionbar.command.enable")
    private val disable = config.getString("actionbar.command.disable")
    override fun command(): String { return "actionbar" }

    override fun allowedConsole(): Boolean { return false }

    override fun description(): String { return "在actionbar查看MoeFilter状态." }

    override fun usage(): List<String> { return listOf("/moefilter actionbar") }

    override fun permission(): String { return "moefilter.notification" }

    override fun execute(sender: CommandSender, args: Array<out String>?) {
        if (Notifications.toggleSpyNotificationPlayer(sender as ProxiedPlayer)) { MessageUtil.sendMessage(sender, MessageUtil.colorizeMiniMessage("$prefix$enable"))
        } else { MessageUtil.sendMessage(sender, MessageUtil.colorizeMiniMessage("$prefix$disable")) }
    }

    override fun tabComplete(sender: CommandSender): MutableMap<Int, List<String>> { return mutableMapOf() }
}