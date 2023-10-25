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

package catmoe.fallencrystal.translation.server.bungee

import catmoe.fallencrystal.translation.platform.Platform
import catmoe.fallencrystal.translation.platform.ProxyPlatform
import catmoe.fallencrystal.translation.server.ServerGetter
import catmoe.fallencrystal.translation.server.ServerInstance
import catmoe.fallencrystal.translation.server.TranslateServer
import net.md_5.bungee.BungeeCord

@Platform(ProxyPlatform.BUNGEE)
class BungeeServerGetter : ServerGetter {
    override fun getServer(name: String): TranslateServer? {
        return try { TranslateServer(BungeeServer(BungeeCord.getInstance().getServerInfo(name))) } catch(_: Exception) { null }
    }

    override fun init() {
        BungeeCord.getInstance().servers.forEach { ServerInstance.addServer(TranslateServer(BungeeServer(it.value))) }
    }
}