/*
 * Copyright (C) 2023-2023. CatMoe / MoeFilter Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package catmoe.fallencrystal.moefilter.api.command.impl

import catmoe.fallencrystal.moefilter.api.command.CommandManager
import catmoe.fallencrystal.moefilter.api.command.ICommand
import catmoe.fallencrystal.moefilter.common.state.StateManager
import catmoe.fallencrystal.moefilter.network.bungee.initializer.MoeChannelHandler
import catmoe.fallencrystal.moefilter.network.limbo.handler.LimboHandler
import catmoe.fallencrystal.moefilter.network.limbo.handler.LimboLoader
import catmoe.fallencrystal.moefilter.util.message.v2.MessageUtil
import catmoe.fallencrystal.moefilter.util.message.v2.packet.type.MessagesType
import catmoe.fallencrystal.translation.command.annotation.MoeCommand
import catmoe.fallencrystal.translation.command.annotation.misc.DescriptionType
import net.md_5.bungee.api.CommandSender
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.UnknownHostException
import kotlin.math.roundToInt

/*
@Command("limbo")
@CommandPermission("moefilter.limbo")
@CommandDescription(DescriptionFrom.STRING, "Lookup MoeLimbo status")
@CommandUsage([
    "/moefilter limbo list", "/moefilter limbo kick <Address>", "/moefilter limbo kick -a"
])
 */
@MoeCommand(
    name = "limbo",
    permission = "moefilter.limbo",
    descType = DescriptionType.STRING,
    descValue = "Lookup MoeLimbo status",
    usage = ["/moefilter limbo list", "/moefilter limbo kick <Address>", "/moefilter limbo kick -a"]
)
class LimboCommand : ICommand {
    override fun execute(sender: CommandSender, args: Array<out String>) {
        if (args.size < 2) { MessageUtil.sendMessage("<red>请键入子命令", MessagesType.CHAT, sender); return }
        val a = args[1]
        val s = MessageUtil.argsBuilder(1, args)
        when (a) {
            "list" -> {
                if (StateManager.inAttack.get() && !s.endsWith("-c") && !s.endsWith("--confirm")) {
                    MessageUtil.sendMessage("<red>您不能在攻击期间使用此命令 如需强制执行 请在结尾加上 --confirm", MessagesType.CHAT, sender); return
                }
                MessageUtil.sendMessage(" <aqua>连接列表: <aqua>[${LimboLoader.connections.size}]", MessagesType.CHAT, sender)
                try {
                    for (it in LimboLoader.connections) {
                        try {
                            val t = listOf(
                                "<aqua>在线: ${if (it.channel.isActive) "<green>是" else "<red>否"}",
                                "<aqua>用户名: <yellow>${it.profile.username} <aqua>版本: ${it.version?.name}",
                                "<aqua>From: <yellow>${it.host?.hostName}:${it.host?.port}",
                                "<aqua>有效握手: ${if (MoeChannelHandler.sentHandshake.getIfPresent(it.channel) == null) "<red>否" else "<green>是"}",
                                "<aqua>Brand: ${it.brand}",
                                "<aqua>Location: <yellow>X: ${it.location?.x?.roundToInt()} Y: ${it.location?.y?.roundToInt()} Z: ${it.location?.z?.roundToInt()}"
                            ).joinToString("<reset><newline>")
                            val ad = (it.address as InetSocketAddress).address.hostAddress
                            val m = " <hover:show_text:\'$t\'><yellow>$ad</hover>  " +
                                    "<yellow>[<red><click:suggest_command:'/mf limbo kick $ad'>Kick</click></red>]"
                            MessageUtil.sendMessage(m, MessagesType.CHAT, sender)
                        } catch (_: NullPointerException) {}
                    }
                } catch (_: ConcurrentModificationException) {
                    MessageUtil.sendMessage("<red>遇到了并发修改异常..<newline>服务器可能正在遭受攻击 请稍后再试", MessagesType.CHAT, sender)
                }
            }
            "kick" -> {
                if (StateManager.inAttack.get() && !s.endsWith("-c") && !s.endsWith("--confirm")) {
                    MessageUtil.sendMessage("<red>您不能在攻击期间使用此命令 如需强制执行 请在结尾加上 --confirm", MessagesType.CHAT, sender); return
                }
                if (s.contains("-a") || s.contains("--all")) {
                    val r: MutableCollection<LimboHandler> = ArrayList()
                    for (it in LimboLoader.connections) { try { it.channel.close() } catch (_: NullPointerException) {}; r.add(it) }
                    LimboLoader.connections.removeAll(r.toSet())
                    MessageUtil.sendMessage("<green>All connection kicked.", MessagesType.CHAT, sender)
                    return
                }
                val ad = try { InetAddress.getByName(args[2]) } catch (_: IllegalArgumentException) {
                    MessageUtil.sendMessage("<red>Cannot parse address", MessagesType.CHAT, sender)
                    return }
                catch (_: IndexOutOfBoundsException) {}
                catch (_: UnknownHostException) {
                    MessageUtil.sendMessage("<red>Cannot parse address", MessagesType.CHAT, sender)
                    return
                }
                var c: LimboHandler? = null
                LimboLoader.connections.forEach { if ((it.address as InetSocketAddress).address == ad) { c=it; return@forEach } }
                if (c == null) { MessageUtil.sendMessage("<red>Target not found.", MessagesType.CHAT, sender) }
                else {
                    c?.channel?.close(); LimboLoader.connections.remove(c)
                    MessageUtil.sendMessage("<green>Successfully kicked.", MessagesType.CHAT, sender)
                }
            }
        }
    }

    /*
    override fun tabComplete(sender: CommandSender): MutableMap<Int, List<String>> {
        return mapOf(
            1 to listOf("list", "kick"),
            2 to listOf("<Address>", "-a", "--all", "-c", "--confirm"),
            3 to listOf("-c", "--confirm")
        ) as MutableMap<Int, List<String>>
    }
     */

    override fun tabComplete(sender: CommandSender, args: Array<out String>): MutableCollection<String>? {
        return when (args.size) {
            2 -> CommandManager.sortContext(args[1], listOf("list", "kick").toMutableList())
            3 -> {
                when (args[1].lowercase()) {
                    "list" -> if (StateManager.inAttack.get()) mutableListOf("-c", "--confirm") else mutableListOf()
                    "kick" -> mutableListOf("<Address>", "-a", "--all")
                    else -> null
                }
            }
            4 -> if (StateManager.inAttack.get() && args[1].equals("kick", ignoreCase = true)) mutableListOf("-c", "--confirm") else mutableListOf()
            else -> null
        }
    }
}