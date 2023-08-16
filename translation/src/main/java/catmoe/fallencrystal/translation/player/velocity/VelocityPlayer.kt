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

package catmoe.fallencrystal.translation.player.velocity

import catmoe.fallencrystal.translation.platform.Platform
import catmoe.fallencrystal.translation.platform.ProxyPlatform
import catmoe.fallencrystal.translation.player.PlatformPlayer
import catmoe.fallencrystal.translation.server.PlatformServer
import catmoe.fallencrystal.translation.server.TranslateServer
import catmoe.fallencrystal.translation.server.velocity.VelocityServer
import catmoe.fallencrystal.translation.utils.component.ComponentUtil
import catmoe.fallencrystal.translation.utils.version.Version
import com.velocitypowered.api.proxy.Player
import net.kyori.adventure.text.Component
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.util.*

@Suppress("MemberVisibilityCanBePrivate")
@Platform(ProxyPlatform.VELOCITY)
class VelocityPlayer(val player: Player) : PlatformPlayer {
    override fun getAddress(): SocketAddress {
        return player.remoteAddress
    }

    override fun virtualHost(): InetSocketAddress {
        return player.virtualHost.get()
    }

    override fun getBrand(): String {
        return player.clientBrand ?: ""
    }

    override fun getVersion(): Version {
        return Version.of(player.protocolVersion.protocol)
    }

    override fun getName(): String {
        return player.username
    }

    override fun getUUID(): UUID {
        return player.uniqueId
    }

    override fun isOnlineMode(): Boolean {
        return player.isOnlineMode
    }

    override fun isOnline(): Boolean {
        return player.isActive
    }

    override fun disconnect() {
        player.disconnect(ComponentUtil.parse("<red>Kicked by MoeTranslation"))
    }

    override fun disconnect(reason: Component) {
        player.disconnect(reason)
    }

    override fun send(server: PlatformServer) {
        (if (server is TranslateServer) (server.upstream as VelocityServer) else server as VelocityServer).send(this)
    }
}