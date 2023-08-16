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