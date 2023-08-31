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

package catmoe.fallencrystal.moefilter.api.command.impl

import catmoe.fallencrystal.moefilter.api.command.ICommand
import catmoe.fallencrystal.moefilter.api.event.EventManager
import catmoe.fallencrystal.moefilter.api.event.events.PluginReloadEvent
import catmoe.fallencrystal.translation.command.annotation.MoeCommand
import catmoe.fallencrystal.translation.command.annotation.misc.DescriptionType
import net.md_5.bungee.api.CommandSender

/*
@Command("reload")
@ConsoleCanExecute
@CommandDescription(DescriptionFrom.MESSAGE_PATH, "command.description.reload")
@CommandUsage(["/moefilter reload"])
@CommandPermission("moefilter.reload")
 */
@MoeCommand(
    name = "reload",
    permission = "moefilter.reload",
    usage = ["/moefilter reload"],
    descType = DescriptionType.MESSAGE_CONFIG,
    descValue = "command.description.reload",
    allowConsole = true
)
class ReloadCommand : ICommand {

    override fun execute(sender: CommandSender, args: Array<out String>) { EventManager.triggerEvent(PluginReloadEvent(sender)) }

    override fun tabComplete(sender: CommandSender): MutableMap<Int, List<String>> { return mutableMapOf() }
}