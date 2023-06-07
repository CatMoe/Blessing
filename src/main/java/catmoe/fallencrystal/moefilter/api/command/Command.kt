package catmoe.fallencrystal.moefilter.api.command

import catmoe.fallencrystal.moefilter.common.config.ObjectConfig
import catmoe.fallencrystal.moefilter.util.message.MessageUtil
import catmoe.fallencrystal.moefilter.util.message.MessageUtil.colorizeMiniMessage
import catmoe.fallencrystal.moefilter.util.plugin.FilterPlugin

import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.plugin.TabExecutor

class Command(name: String?, permission: String?, vararg aliases: String?) : net.md_5.bungee.api.plugin.Command(name, permission, *aliases), TabExecutor {

    private val config = ObjectConfig.getMessage() // Message Config
    private val prefix: String = config.getString("prefix")
    private val fullHideCommand = config.getBoolean("command.full-hide-command")

    override fun execute(sender: CommandSender?, args: Array<out String>?) {
        // 当玩家没有权限或未输入任何子命令时  详见 infoCommand 方法.
        if (args.isNullOrEmpty() || !sender!!.hasPermission("moefilter")) { infoCommand(sender!!); return }
        val command = OCommand.getICommand(args[0])
        if (command != null) {
            val permission = command.permission()
            if (sender !is ProxiedPlayer && !command.allowedConsole()) { MessageUtil.sendMessage(sender, colorizeMiniMessage("$prefix${config.getString("command.only-player")}")); return }
            if (!sender.hasPermission(permission)) {
                if (fullHideCommand) {
                    MessageUtil.sendMessage(sender, colorizeMiniMessage("$prefix${config.getString("command.not-found")}")) }
                else {
                    MessageUtil.sendMessage(sender, colorizeMiniMessage("$prefix${config.getString("command.no-permission").replace("[permission]", permission)}")) }
                return
            }
            else { command.execute(sender, args) }
        } else { MessageUtil.sendMessage(sender, colorizeMiniMessage("$prefix${config.getString("command.not-found")}")) } // MessageNotFound
    }

    override fun onTabComplete(sender: CommandSender?, args: Array<out String>?): List<String> {
        if (!sender!!.hasPermission("moefilter")) return listOf(config.getString("command.tabComplete.no-permission"))
        if (args!!.size == 1) { val list = mutableListOf<String>(); OCommand.getCommandList(sender).forEach { if (sender.hasPermission(it.permission())) { list.add(it.command()) } }; return list }
        val command = OCommand.getICommand(args[0])
        return if (command != null) {
            if (!sender.hasPermission(command.permission())) {
               if (!fullHideCommand) { listOf(config.getString("command.tabComplete.no-subcommand-permission").replace("[permission]", command.permission())) } else { listOf() }
            } else command.tabComplete(sender)[args.size - 1] ?: listOf()
        } else { listOf() }
    }

    private fun infoCommand(sender: CommandSender) {
        val version = FilterPlugin.getPlugin()!!.description.version
        val line = if (sender.hasPermission("moefilter")) "  <yellow>使用 <white>/moefilter help <yellow>查看命令列表" else " <white> github.com/CatMoe/MoeFilter"
        val message: List<String> = listOf(
            "<aqua><st><b>                                        ",
            "  &bMoe&fFilter &7- &f$version",
            "",
            line,
            "<aqua><st><b>                                        "
        )
        message.forEach { MessageUtil.sendMessage(sender, colorizeMiniMessage(it)) }
    }

}