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
import catmoe.fallencrystal.moefilter.api.command.annotation.*
import catmoe.fallencrystal.moefilter.api.command.annotation.misc.DescriptionFrom
import catmoe.fallencrystal.moefilter.network.bungee.util.bconnection.ConnectionUtil
import catmoe.fallencrystal.moefilter.network.bungee.util.kick.DisconnectType
import catmoe.fallencrystal.moefilter.network.bungee.util.kick.FastDisconnect
import net.md_5.bungee.UserConnection
import net.md_5.bungee.api.CommandSender

@DebugCommand
@Command("testkick")
@CommandPermission("moefilter.testkick")
@CommandUsage(["/moefilter testkick"])
@CommandDescription(DescriptionFrom.STRING, "testkick")
class TestKickCommand : ICommand {
    override fun execute(sender: CommandSender, args: Array<out String>?) {
        val connection = ConnectionUtil((sender as UserConnection).pendingConnection)
        FastDisconnect.disconnect(connection, DisconnectType.ALREADY_ONLINE)
    }

    override fun tabComplete(sender: CommandSender): MutableMap<Int, List<String>> { return mutableMapOf() }
}