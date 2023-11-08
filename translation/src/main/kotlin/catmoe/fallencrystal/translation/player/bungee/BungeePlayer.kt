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

package catmoe.fallencrystal.translation.player.bungee

import catmoe.fallencrystal.translation.platform.Platform
import catmoe.fallencrystal.translation.platform.ProxyPlatform
import catmoe.fallencrystal.translation.player.PlatformPlayer
import catmoe.fallencrystal.translation.server.PlatformServer
import catmoe.fallencrystal.translation.server.ServerInstance
import catmoe.fallencrystal.translation.server.TranslateServer
import catmoe.fallencrystal.translation.server.bungee.BungeeServer
import catmoe.fallencrystal.translation.utils.version.Version
import io.netty.buffer.Unpooled
import io.netty.channel.Channel
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.connection.InitialHandler
import net.md_5.bungee.netty.ChannelWrapper
import net.md_5.bungee.protocol.DefinedPacket
import net.md_5.bungee.protocol.packet.Kick
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.util.*

@Suppress("MemberVisibilityCanBePrivate")
@Platform(ProxyPlatform.BUNGEE)
class BungeePlayer(val player: ProxiedPlayer): PlatformPlayer {
    override fun getAddress(): SocketAddress { return player.socketAddress }

    var realbrand: String? = null
    var channelWrapper = getWrapper()

    override fun virtualHost(): InetSocketAddress? { return player.pendingConnection.virtualHost }

    override fun getBrand(): String {
        if (realbrand == null) {
            val b = Unpooled.wrappedBuffer((player as InitialHandler).brandMessage.data)
            realbrand = DefinedPacket.readString(b)
            b.release()
        }
        return realbrand ?: ""
    }

    override fun getVersion(): Version {
        return Version.of(player.pendingConnection.version)
    }

    override fun getName(): String { return player.name }

    // TODO: Migrate MessageUtil to translation

    private fun hex(): Boolean { return player.pendingConnection.version >= Version.V1_16.number }

    private fun getBaseComponent(component: Component): BaseComponent {
        return when (hex()) {
            true -> BungeeComponentSerializer.get().serialize(component)[0]
            false -> BungeeComponentSerializer.legacy().serialize(component)[0]
        }
    }

    override fun sendMessage(component: Component) {
        player.sendMessage(ChatMessageType.CHAT, getBaseComponent(component))
    }

    override fun sendActionbar(component: Component) {
        player.sendMessage(ChatMessageType.ACTION_BAR, getBaseComponent(component))
    }

    override fun hasPermission(permission: String): Boolean {
        return player.hasPermission(permission)
    }

    override fun getUniqueId(): UUID {
        return player.uniqueId
    }

    override fun isOnlineMode(): Boolean {
        return player.pendingConnection.isOnlineMode
    }

    override fun isOnline(): Boolean { return player.isConnected }

    override fun disconnect(reason: Component) {
        val serializer = if (getVersion().moreOrEqual(Version.V1_16)) BungeeComponentSerializer.get() else BungeeComponentSerializer.legacy()
        if (player.isConnected) player.unsafe().sendPacket(Kick(serializer.serialize(reason)[0]))
    }

    override fun disconnect() {
        if (channelWrapper == null) { player.unsafe().sendPacket(Kick()); return }
        channelWrapper?.close()
    }

    private fun getWrapper(): ChannelWrapper? {
        return try {
            val i = player as InitialHandler
            val f = i.javaClass.getDeclaredField("ch")
            f.isAccessible=true; f[i] as ChannelWrapper
        } catch (_: Exception) { null }
    }

    override fun channel(): Channel { return channelWrapper!!.handle.pipeline().channel() }

    override fun send(server: PlatformServer) {
        (if (server is TranslateServer) (server.upstream as BungeeServer) else server as BungeeServer).send(this)
    }

    override fun getServer(): TranslateServer {
        val info = player.server.info
        return ServerInstance.getServer(info.name) ?: TranslateServer(BungeeServer(info))
    }
}