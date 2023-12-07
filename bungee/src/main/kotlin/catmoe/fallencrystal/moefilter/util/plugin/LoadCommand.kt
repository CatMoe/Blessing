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

package catmoe.fallencrystal.moefilter.util.plugin

import catmoe.fallencrystal.moefilter.api.command.CommandManager
import catmoe.fallencrystal.moefilter.api.command.impl.*
import catmoe.fallencrystal.moefilter.api.command.impl.test.RegexParseCommand
import catmoe.fallencrystal.moefilter.api.command.impl.test.TestKickCommand
import catmoe.fallencrystal.moefilter.api.command.impl.test.TestWebhookCommand
import catmoe.fallencrystal.moefilter.util.message.v2.MessageUtil
import catmoe.fallencrystal.translation.command.annotation.MoeCommand
import catmoe.fallencrystal.translation.utils.config.LocalConfig
import catmoe.fallencrystal.translation.utils.config.Reloadable
import kotlin.reflect.full.createInstance

class LoadCommand : Reloadable {

    companion object {
        val commands = mutableListOf(
            HelpCommand::class,
            ToggleStatisticsCommand::class,
            ReloadCommand::class,
            TestKickCommand::class,
            TestWebhookCommand::class,
            LockdownCommand::class,
            DropFirewallCommand::class,
            StatisticsCommand::class,
            RegexParseCommand::class,
            LimboCommand::class
        )
    }

    override fun reload() { CommandManager.dropAll(); loadCommand() }

    private fun loadCommand() {
        val debug = LocalConfig.getConfig().getBoolean("debug")
        for (c in commands) {
            if (!c.java.isAnnotationPresent(MoeCommand::class.java) || (!debug && c.java.getAnnotation(MoeCommand::class.java).debug)) continue
            try {
                CommandManager.register(c.createInstance())
            } catch (e: Exception) {
                MessageUtil.logWarn("[MoeFilter] A exception occurred when registering command. Please report this issue to developer. stack trace:")
                e.printStackTrace(); continue
            }
        }
    }
}