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

import catmoe.fallencrystal.translation.TranslationLoader
import catmoe.fallencrystal.translation.platform.Platform
import catmoe.fallencrystal.translation.platform.ProxyPlatform
import catmoe.fallencrystal.translation.player.PlatformPlayer
import catmoe.fallencrystal.translation.player.PlayerInstance
import catmoe.fallencrystal.translation.player.TranslatePlayer
import catmoe.fallencrystal.translation.player.bungee.BungeePlayer
import catmoe.fallencrystal.translation.server.PlatformServer
import net.md_5.bungee.api.config.ServerInfo
import java.net.InetSocketAddress

@Suppress("MemberVisibilityCanBePrivate")
@Platform(ProxyPlatform.BUNGEE)
class BungeeServer(val server: ServerInfo) : PlatformServer {
    override fun getAddress(): InetSocketAddress {
        return server.socketAddress as InetSocketAddress
    }

    override fun getName(): String {
        return server.name
    }

    override fun getOnlinePlayers(): Collection<TranslatePlayer> {
        val a: MutableCollection<TranslatePlayer> = ArrayList()
        server.players.forEach { PlayerInstance.getPlayer(it.uniqueId)?.let { it1 -> a.add(it1) } }
        return a
    }

    override fun send(player: PlatformPlayer) {
        if (TranslationLoader.canAccess(BungeePlayer::class)) {
            val p = if (player is TranslatePlayer) (player.upstream as BungeePlayer).player else (player as BungeePlayer).player
            p.connect(server)
        }
    }
}