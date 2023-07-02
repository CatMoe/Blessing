package catmoe.fallencrystal.moefilter.api.command

import catmoe.fallencrystal.moefilter.MoeFilter
import catmoe.fallencrystal.moefilter.api.command.CommandManager.getCommandList
import catmoe.fallencrystal.moefilter.api.command.CommandManager.getParsedCommand
import catmoe.fallencrystal.moefilter.common.config.LocalConfig
import catmoe.fallencrystal.moefilter.util.message.v2.MessageUtil
import catmoe.fallencrystal.moefilter.util.message.v2.packet.type.MessagesType
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.plugin.TabExecutor

class CommandHandler(name: String?, permission: String?, vararg aliases: String?) : net.md_5.bungee.api.plugin.Command(name, permission, *aliases), TabExecutor {

    private val config = LocalConfig.getMessage() // Message Config
    private val prefix: String = config.getString("prefix")
    private val fullHideCommand = config.getBoolean("command.full-hide-command")

    override fun execute(sender: CommandSender?, args: Array<out String>?) {
        // 当玩家没有权限或未输入任何子命令时  详见 infoCommand 方法.
        if (args.isNullOrEmpty() || !sender!!.hasPermission("moefilter")) { infoCommand(sender!!); return }
        val command = CommandManager.getICommand(args[0])
        if (command != null) {
            val parsedInfo = getParsedCommand(command)!!
            val permission = parsedInfo.permission
            if (sender !is ProxiedPlayer && !parsedInfo.allowConsole) { MessageUtil.sendMessage("$prefix${config.getString("command.only-player")}", MessagesType.CHAT, sender); return }
            if (!sender.hasPermission(permission)) {
                if (fullHideCommand) { MessageUtil.sendMessage("$prefix${config.getString("command.not-found")}", MessagesType.CHAT, sender) }
                else { MessageUtil.sendMessage("$prefix${config.getString("command.no-permission").replace("[permission]", permission)}", MessagesType.CHAT, sender) }; return
            }
            else { command.execute(sender, args) }
        } else { MessageUtil.sendMessage("$prefix${config.getString("command.not-found")}", MessagesType.CHAT, sender) }
    }

    override fun onTabComplete(sender: CommandSender?, args: Array<out String>?): List<String> {
        val noPermission = if (config.getString("command.tabComplete.no-permission").isNotEmpty()) { listOf(config.getString("command.tabComplete.no-permission")) } else listOf()
        val noSubPermission = if (config.getString("command.tabComplete.no-subcommand-permission").isNotEmpty()) listOf(config.getString("command.tabComplete.no-subcommand-permission").replace("[permission]", permission)) else listOf()
        if (!sender!!.hasPermission("moefilter")) return noPermission
        if (args!!.size == 1) { val list = mutableListOf<String>(); getCommandList(sender).forEach { list.add(getParsedCommand(it)!!.command) }; return list }
        val command = CommandManager.getICommand(args[0])
        return if (command != null) {
            val permission = getParsedCommand(command)!!.permission
            if (!sender.hasPermission(permission)) { if (!fullHideCommand) { noSubPermission } else { listOf() } } else command.tabComplete(sender)[args.size - 1] ?: listOf()
        } else { listOf() }
    }

    private fun infoCommand(sender: CommandSender) {
        val version = MoeFilter.instance.description.version
        val line = if (sender.hasPermission("moefilter")) "  <yellow>使用 <white>/moefilter help <yellow>查看命令列表" else " <white> github.com/CatMoe/MoeFilter"
        val message: List<String> = listOf(
            "",
            "  <aqua>Moe<white>Filter <gray>- <white>$version",
            "",
            line,
            ""
        )
        MessageUtil.sendMessage(message.joinToString("<reset><newline>"), MessagesType.CHAT, sender)
    }

}