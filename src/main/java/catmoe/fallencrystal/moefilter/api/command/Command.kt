package catmoe.fallencrystal.moefilter.api.command

import catmoe.fallencrystal.moefilter.MoeFilter
import catmoe.fallencrystal.moefilter.util.MessageUtil
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.plugin.TabExecutor

class Command(name: String?, permission: String?, vararg aliases: String?) : net.md_5.bungee.api.plugin.Command(name, permission, *aliases), TabExecutor {

    val plugin = MoeFilter()

    override fun execute(sender: CommandSender?, args: Array<out String>?) {
        // 当玩家没有权限或未输入任何子命令时  详见 infoCommand 方法.
        if (args.isNullOrEmpty() || !sender!!.hasPermission("moefilter")) { infoCommand(sender!!); return }
        val command = CommandList.getICommand(args[0])
        if (command != null) {
            val permission = command.permission()
            if (!sender.hasPermission(permission)) { MessageUtil.sendMessage(sender, "  &c缺少权限: $permission") } else { command.execute(sender, args) }
        } else { MessageUtil.sendMessage(sender, "COMMAND_NOT_FOUND") }
    }

    override fun onTabComplete(sender: CommandSender?, args: Array<out String>?): List<String> {
        if (args!!.size == 1) return CommandList.commandList()
        val command = CommandList.getICommand(args[1])
        return if (args.size == 2 && command != null) {
            val map = command.tabComplete()
            map[args.size - 1] ?: listOf()
        } else { listOf() }
    }

    private fun infoCommand(sender: CommandSender) {
        val version = MoeFilter().getInstance().description.version
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