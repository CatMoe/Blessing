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

package catmoe.fallencrystal.moefilter.api.command.impl.test

import catmoe.fallencrystal.moefilter.api.command.ICommand
import catmoe.fallencrystal.moefilter.util.message.v2.MessageUtil
import catmoe.fallencrystal.moefilter.util.message.v2.packet.type.MessagesType
import catmoe.fallencrystal.translation.command.annotation.MoeCommand
import catmoe.fallencrystal.translation.command.annotation.misc.DescriptionType
import net.md_5.bungee.api.CommandSender
import java.util.regex.PatternSyntaxException

/*
@Command("regex")
@CommandPermission("moefilter.regex")
@CommandDescription(DescriptionFrom.STRING, "Test regex through command.")
@DebugCommand
@ConsoleCanExecute
 */
@MoeCommand(
    name = "regex",
    permission = "moefilter.regex",
    descType = DescriptionType.STRING,
    descValue = "Test regex through command",
    usage = ["/moefilter regex <Regex> <Input>"],
    debug = true,
    allowConsole = true
)
class RegexParseCommand : ICommand {
    override fun execute(sender: CommandSender, args: Array<out String>) {
        try {
            val regex = args[1].toRegex()
            val result: MutableCollection<String> = ArrayList()
            val parse = MessageUtil.argsBuilder(2, args)
            MessageUtil.sendMessage("Regex: ${regex.pattern}, Test: $parse", MessagesType.CHAT, sender)
            regex.findAll(parse).forEach { result.add(it.value); MessageUtil.sendMessage("Result founded: ${it.value}", MessagesType.CHAT, sender) }
        }
        catch (_: IndexOutOfBoundsException) { MessageUtil.sendMessage("Syntax error.", MessagesType.ACTION_BAR, sender) }
        catch (p: PatternSyntaxException) { MessageUtil.logInfo(p.localizedMessage) }
    }

    /*
    override fun tabComplete(sender: CommandSender): MutableMap<Int, List<String>> {
        val map: MutableMap<Int, List<String>> = mutableMapOf()
        map[1] = listOf("<Regex>")
        map[2] = listOf("<Parse>")
        return map
    }
     */
    override fun tabComplete(sender: CommandSender, args: Array<out String>): MutableCollection<String> {
        return when (args.size) {
            2 -> mutableListOf("<Regex>")
            else -> mutableListOf("<Parse>")
        }
    }
}