/*
 * Copyright (C) 2023-2024. CatMoe / Blessing Contributors
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

package net.miaomoe.blessing.bungee.fallback

import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer
import net.md_5.bungee.api.Callback
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.ServerPing
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.connection.PendingConnection
import net.md_5.bungee.api.event.ProxyPingEvent
import net.miaomoe.blessing.fallback.handler.FallbackHandler
import net.miaomoe.blessing.fallback.handler.motd.FallbackMotdHandler
import net.miaomoe.blessing.fallback.handler.motd.MotdInfo
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.util.*

object BungeeMotdAdapter : FallbackMotdHandler {

    private val bungee = ProxyServer.getInstance()
    private val proxyName = bungee.name
    private val defaultDescription = TextComponent.fromLegacy("§1Another $proxyName server")

    override fun handle(handler: FallbackHandler): MotdInfo {
        val adapter = PingConnectionAdapter(handler)
        val maxOnline = bungee.config.playerLimit
        val default = ServerPing(
            ServerPing.Protocol(bungee.name, handler.version.protocolId),
            ServerPing.Players(maxOnline, bungee.onlineCount, null),
            defaultDescription,
            bungee.config.faviconObject
        )
        var output: ServerPing? = default
        val callBack= Callback<ServerPing> { ping, throwable ->
            if (throwable != null) { output=null; return@Callback }
            output = callEvent(adapter, ping)
        }
        callBack.done(default, null)
        return (output ?: default).convert()
    }

    private fun callEvent(adapter: PingConnectionAdapter, default: ServerPing): ServerPing {
        var output: ServerPing? = null
        val callback = Callback<ProxyPingEvent> { result, throwable ->
            output = if (throwable != null) null else result.response
        }
        bungee.pluginManager.callEvent(ProxyPingEvent(adapter, default, callback))
        return output ?: default
    }

    private fun ServerPing.convert(): MotdInfo {
        val protocol = this.version.let { MotdInfo.VersionInfo(it.name, it.protocol) }
        val players = this.players.let {
            val samples: List<MotdInfo.Sample> = it.sample
                ?.takeUnless { sample -> sample.isEmpty() }
                ?.map { sample -> MotdInfo.Sample(sample.uniqueId, sample.name) }
                ?: listOf()
            MotdInfo.PlayerInfo(it.max, it.online, samples)
        }
        val modItems = this.modinfo.modList
            .takeUnless { it.isEmpty() }
            ?.map { item -> MotdInfo.ModItem(item.modid, item.version) }
        val modType = this.modinfo
            .takeUnless { it.type == "FML" && modItems == null }
            ?.let { type -> MotdInfo.ModInfo(type.type, modItems ?: listOf()) }
        val description = BungeeComponentSerializer.get().deserialize(arrayOf(this.descriptionComponent))
        val favicon = this.faviconObject?.let { item -> MotdInfo.Favicon(item.encoded) }
        return MotdInfo(protocol, players, description, favicon, modType)
    }

    @Suppress("OVERRIDE_DEPRECATION", "MemberVisibilityCanBePrivate")
    class PingConnectionAdapter(
        private val fallback: FallbackHandler,
        var fakeLegacy: Boolean = true
    ) : PendingConnection {
        companion object {
            @JvmStatic
            private val exception = UnsupportedOperationException("Ping adapter doesn't support it.")
        }

        override fun getAddress() = fallback.address
        override fun isConnected() = fallback.channel.isActive
        override fun getSocketAddress(): SocketAddress = fallback.channel.remoteAddress()
        override fun isLegacy() = fakeLegacy // fix protocolize
        override fun getName() = fallback.toString()
        override fun getVersion() = fallback.version.protocolId
        override fun getVirtualHost(): InetSocketAddress = fallback.destination!!
        override fun disconnect(reason: String?) = throw exception
        override fun disconnect(vararg p0: BaseComponent?) = throw exception
        override fun disconnect(p0: BaseComponent?) = throw exception
        override fun unsafe() = throw exception
        override fun getListener() = throw exception
        override fun getUUID() = throw exception
        override fun getUniqueId() = throw exception
        override fun setUniqueId(p0: UUID?) = throw exception
        override fun isOnlineMode() = throw exception
        override fun setOnlineMode(p0: Boolean) = throw exception
    }

}