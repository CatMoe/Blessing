/*
 * Copyright 2023. CatMoe / FallenCrystal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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