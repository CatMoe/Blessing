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