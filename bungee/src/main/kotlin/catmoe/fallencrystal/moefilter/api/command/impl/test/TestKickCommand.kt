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
import catmoe.fallencrystal.moefilter.network.bungee.util.bconnection.ConnectionUtil
import catmoe.fallencrystal.moefilter.network.common.kick.DisconnectType
import catmoe.fallencrystal.moefilter.network.common.kick.FastDisconnect
import catmoe.fallencrystal.translation.command.annotation.MoeCommand
import catmoe.fallencrystal.translation.command.annotation.misc.DescriptionType
import net.md_5.bungee.UserConnection
import net.md_5.bungee.api.CommandSender

/*
@DebugCommand
@Command("testkick")
@CommandPermission("moefilter.testkick")
@CommandUsage(["/moefilter testkick"])
@CommandDescription(DescriptionFrom.STRING, "testkick")
 */
@Suppress("SpellCheckingInspection")
@MoeCommand(
    name = "testkick",
    permission = "moefilter.testkick",
    usage = ["/moefilter testkick"],
    descType = DescriptionType.STRING,
    descValue = "testkick",
    debug = true
)
class TestKickCommand : ICommand {
    override fun execute(sender: CommandSender, args: Array<out String>) {
        val connection = ConnectionUtil((sender as UserConnection).pendingConnection)
        FastDisconnect.disconnect(connection, DisconnectType.ALREADY_ONLINE)
    }

    override fun tabComplete(sender: CommandSender, args: Array<out String>): MutableCollection<String>? {
        return null
    }
}