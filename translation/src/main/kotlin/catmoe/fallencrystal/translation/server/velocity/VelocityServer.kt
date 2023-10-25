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

package catmoe.fallencrystal.translation.server.velocity

import catmoe.fallencrystal.translation.TranslationLoader
import catmoe.fallencrystal.translation.platform.Platform
import catmoe.fallencrystal.translation.platform.ProxyPlatform
import catmoe.fallencrystal.translation.player.PlatformPlayer
import catmoe.fallencrystal.translation.player.PlayerInstance
import catmoe.fallencrystal.translation.player.TranslatePlayer
import catmoe.fallencrystal.translation.player.velocity.VelocityPlayer
import catmoe.fallencrystal.translation.server.PlatformServer
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.proxy.server.RegisteredServer
import java.net.InetSocketAddress

@Platform(ProxyPlatform.VELOCITY)
class VelocityServer(val server: RegisteredServer) : PlatformServer {
    override fun getAddress(): InetSocketAddress {
        return server.serverInfo.address
    }

    override fun getName(): String {
        return server.serverInfo.name
    }

    override fun getOnlinePlayers(): Collection<TranslatePlayer> {
        val a: MutableCollection<TranslatePlayer> = ArrayList()
        server.playersConnected.forEach { PlayerInstance.getPlayer(it.uniqueId)?.let { it1 -> a.add(it1) } }
        return a
    }

    override fun send(player: PlatformPlayer) {
        if (TranslationLoader.canAccess(VelocityPlayer::class)) {
            val p = if (player is TranslatePlayer) (player.upstream as VelocityPlayer).player else (player as VelocityPlayer).player
            val plugin = TranslationLoader.instance.loader.loader
            val s = (plugin.getProxyServer().obj as ProxyServer).scheduler
            val b = p.createConnectionRequest(server)
            s.buildTask(plugin, b::fireAndForget).schedule()
        }
    }
}