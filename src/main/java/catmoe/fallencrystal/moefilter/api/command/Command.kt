package catmoe.fallencrystal.moefilter.api.command

import catmoe.fallencrystal.moefilter.common.config.ObjectConfig
import catmoe.fallencrystal.moefilter.util.plugin.FilterPlugin
import catmoe.fallencrystal.moefilter.util.message.MessageUtil
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.plugin.TabExecutor

class Command(name: String?, permission: String?, vararg aliases: String?) : net.md_5.bungee.api.plugin.Command(name, permission, *aliases), TabExecutor {

    val messageConfig = ObjectConfig.getMessage()
    val prefix: String = messageConfig.getString("prefix")

    override fun execute(sender: CommandSender?, args: Array<out String>?) {
        // 当玩家没有权限或未输入任何子命令时  详见 infoCommand 方法.
        if (args.isNullOrEmpty() || !sender!!.hasPermission("moefilter")) { infoCommand(sender!!); return }
        val command = OCommand.getICommand(args[0])
        if (command != null) {
            val permission = command.permission()
            if (sender !is ProxiedPlayer && !command.allowedConsole()) return
            if (!sender.hasPermission(permission)) { MessageUtil.sendMessage(sender, "$prefix${messageConfig.getString("command.no-permission").replace("[permission]", permission)}") }
            else { command.execute(sender, args) }
        } else { MessageUtil.sendMessage(sender, "$prefix${messageConfig.getString("command.not-found")}") } // MessageNotFound
    }

    override fun onTabComplete(sender: CommandSender?, args: Array<out String>?): List<String> {
        if (args!!.size == 1) return OCommand.commandList()
        val command = OCommand.getICommand(args[1])
        return if (args.size == 2 && command != null) {
            val map = command.tabComplete()
            map[args.size - 1] ?: listOf()
        } else { listOf() }
    }

    private fun infoCommand(sender: CommandSender) {
        val version = FilterPlugin.getPlugin()!!.description.version
        val line = if (sender.hasPermission("moefilter")) "  &e使用 &f/moefilter help &e查看命令列表" else "  &egithub.com/CatMoe/MoeFilter"
        val message: List<String> = listOf(
            "&b&m&l                                                            ",
            "  &bMoe&fFilter &7- &f$version",
            "",
            line,
            "&b&m&l                                                            "
        )
        message.forEach { MessageUtil.sendMessage(sender, it) }
    }

}