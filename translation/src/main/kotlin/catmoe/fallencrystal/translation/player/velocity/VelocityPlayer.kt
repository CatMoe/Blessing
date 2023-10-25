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

package catmoe.fallencrystal.translation.player.velocity

import catmoe.fallencrystal.translation.platform.Platform
import catmoe.fallencrystal.translation.platform.ProxyPlatform
import catmoe.fallencrystal.translation.player.PlatformPlayer
import catmoe.fallencrystal.translation.server.PlatformServer
import catmoe.fallencrystal.translation.server.ServerInstance
import catmoe.fallencrystal.translation.server.TranslateServer
import catmoe.fallencrystal.translation.server.velocity.VelocityServer
import catmoe.fallencrystal.translation.utils.component.ComponentUtil
import catmoe.fallencrystal.translation.utils.version.Version
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.proxy.connection.client.ConnectedPlayer
import io.netty.channel.Channel
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

    override fun sendMessage(component: Component) { player.sendMessage(component) }

    override fun hasPermission(permission: String): Boolean {
        return player.hasPermission(permission)
    }

    override fun getUniqueId(): UUID {
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

    override fun sendActionbar(component: Component) {
        player.sendActionBar(component)
    }

    override fun channel(): Channel {
        // D: For this I imported the entire velocity
        return (player as ConnectedPlayer).connection.channel
    }

    override fun getServer(): TranslateServer {
        val info = player.currentServer.get().server
        return ServerInstance.getServer(info.serverInfo.name) ?: TranslateServer(VelocityServer(info))
    }
}