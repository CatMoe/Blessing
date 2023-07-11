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
import catmoe.fallencrystal.moefilter.api.command.impl.HelpCommand
import catmoe.fallencrystal.moefilter.api.command.impl.ReloadCommand
import catmoe.fallencrystal.moefilter.api.command.impl.ToggleNotificationCommand
import catmoe.fallencrystal.moefilter.api.command.impl.test.TestKickCommand
import catmoe.fallencrystal.moefilter.api.command.impl.test.gui.TestGuiCommand

class LoadCommand {

    fun load(){ loadCommand() }

    fun reload() { CommandManager.dropAll(); loadCommand() }

    private fun loadCommand() {
        CommandManager.register(HelpCommand())
        CommandManager.register(ToggleNotificationCommand())

        // All debug command managers by @DebugCommand annotation
        CommandManager.register(ReloadCommand())
        CommandManager.register(TestKickCommand())
        CommandManager.register(TestGuiCommand())
    }
}