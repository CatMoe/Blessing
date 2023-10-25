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

package catmoe.fallencrystal.translation.executor.bungee

import catmoe.fallencrystal.translation.executor.CommandConsole
import catmoe.fallencrystal.translation.logger.CubeLogger
import com.google.common.base.Preconditions
import net.kyori.adventure.text.Component
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.ProxyServer
import java.util.logging.Level

class BungeeConsole(val console: CommandSender) : CommandConsole() {

    init {
        //if (console != ProxyServer.getInstance().console) throw IllegalArgumentException("Target isn't BungeeCord console.")
        Preconditions.checkArgument(console == ProxyServer.getInstance().console, "Target isn't BungeeCord console.")
    }

    override fun getName(): String {
        return "CONSOLE"
    }

    override fun sendMessage(component: Component) {
        CubeLogger.log(Level.INFO, component)
    }

    override fun hasPermission(permission: String): Boolean {
        return true
    }

}