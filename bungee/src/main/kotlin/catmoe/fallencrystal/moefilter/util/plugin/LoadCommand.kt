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

package catmoe.fallencrystal.moefilter.util.plugin

import catmoe.fallencrystal.moefilter.api.command.CommandManager
import catmoe.fallencrystal.moefilter.api.command.impl.*
import catmoe.fallencrystal.moefilter.api.command.impl.test.RegexParseCommand
import catmoe.fallencrystal.moefilter.api.command.impl.test.TestKickCommand
import catmoe.fallencrystal.moefilter.api.command.impl.test.TestWebhookCommand
import catmoe.fallencrystal.moefilter.api.command.impl.test.gui.TestGuiCommand
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
            TestGuiCommand::class,
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