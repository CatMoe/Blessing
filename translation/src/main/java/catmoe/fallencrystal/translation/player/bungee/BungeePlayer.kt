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

package catmoe.fallencrystal.translation.player.bungee

import catmoe.fallencrystal.translation.platform.Platform
import catmoe.fallencrystal.translation.platform.ProxyPlatform
import catmoe.fallencrystal.translation.player.PlatformPlayer
import catmoe.fallencrystal.translation.server.PlatformServer
import catmoe.fallencrystal.translation.server.TranslateServer
import catmoe.fallencrystal.translation.server.bungee.BungeeServer
import catmoe.fallencrystal.translation.utils.component.ComponentUtil
import catmoe.fallencrystal.translation.utils.version.Version
import io.netty.buffer.Unpooled
import net.kyori.adventure.text.Component
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

    override fun getUUID(): UUID {
        return player.uniqueId
    }

    override fun isOnlineMode(): Boolean {
        return player.pendingConnection.isOnlineMode
    }

    override fun isOnline(): Boolean { return player.isConnected }

    override fun disconnect(reason: Component) {
        if (player.isConnected) player.unsafe().sendPacket(Kick(ComponentUtil.toGson(reason)))
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

    override fun send(server: PlatformServer) {
        (if (server is TranslateServer) (server.upstream as BungeeServer) else server as BungeeServer).send(this)
    }
}