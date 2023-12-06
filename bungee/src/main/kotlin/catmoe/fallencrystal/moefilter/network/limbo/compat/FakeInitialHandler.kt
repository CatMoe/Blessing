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

package catmoe.fallencrystal.moefilter.network.limbo.compat

import catmoe.fallencrystal.moefilter.network.limbo.compat.converter.PingConverter
import catmoe.fallencrystal.moefilter.network.limbo.handler.LimboLoader
import catmoe.fallencrystal.translation.utils.version.Version
import io.netty.channel.ChannelHandlerContext
import net.md_5.bungee.BungeeCord
import net.md_5.bungee.api.Callback
import net.md_5.bungee.api.ServerPing
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.config.ListenerInfo
import net.md_5.bungee.api.connection.Connection
import net.md_5.bungee.api.connection.PendingConnection
import net.md_5.bungee.api.event.ProxyPingEvent
import net.md_5.bungee.netty.ChannelWrapper
import net.md_5.bungee.netty.PacketHandler
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.util.*


@Suppress("OVERRIDE_DEPRECATION", "unused", "MemberVisibilityCanBePrivate")
class FakeInitialHandler(
    val ctx: ChannelHandlerContext
) : PacketHandler(), PendingConnection, LimboCompat {

    val ch = ChannelWrapper(ctx)
    private val channel = ctx.channel()
    private val pipeline = channel.pipeline()
    private val address = channel.remoteAddress()
    private val unsafe = Connection.Unsafe {
        packet ->
        throw UnsupportedOperationException("Failed to write packet ${packet.javaClass.simpleName}: MoeLimbo not supported write DefinedPacket.")
    }
    var username: String? = null
    var v: Version? = null
    var connectionFrom: InetSocketAddress? = null

    override fun toString(): String { return "[ $address ] <-> MoeLimbo" }

    @Deprecated("Use getSocketAddress() as InetSocketAddress")
    override fun getAddress(): InetSocketAddress { return address as InetSocketAddress }
    override fun getSocketAddress(): SocketAddress { return address }
    override fun disconnect(ignore: String?) { channel.close() }
    override fun disconnect(vararg ignore: BaseComponent?) { channel.close() }
    override fun disconnect(ignore: BaseComponent?) { channel.close() }
    override fun isConnected(): Boolean { return channel.isActive }
    override fun unsafe(): Connection.Unsafe { return unsafe }
    override fun getName(): String? { return username }
    override fun getVersion(): Int { return (v ?: Version.UNDEFINED).number }
    override fun getVirtualHost(): InetSocketAddress? { return connectionFrom }
    override fun getListener(): ListenerInfo? { return null }
    @Deprecated("Use getUniqueId")
    override fun getUUID(): String { throw UnsupportedOperationException() }
    override fun getUniqueId(): UUID { throw UnsupportedOperationException() }
    override fun setUniqueId(p0: UUID?) { throw UnsupportedOperationException() }
    override fun isOnlineMode(): Boolean { return false }
    override fun setOnlineMode(p0: Boolean) { throw UnsupportedOperationException() }

    var fakeLegacy = true
    override fun isLegacy(): Boolean { return /* fakeLegacy */ true } // Still always true -- Prevent protocolize inject pipeline.

    @Suppress("DEPRECATION")
    override fun handlePing(host: InetSocketAddress, version: Version): PingConverter {
        LimboLoader.debug("Try to call ProxyPingEvent for bungee")
        fakeLegacy=false
        var pingResult = ""
        val pingBack = Callback<ServerPing> {
            r, t ->
            if (t!= null) { channel.close(); return@Callback }
            fakeLegacy=true
            val callback = Callback<ProxyPingEvent> {
                result, throwable ->
                fakeLegacy=false
                if (throwable != null) channel.close()
                val json = BungeeCord.getInstance().gson.toJson(result.response)
                pingResult=json
            }
            BungeeCord.getInstance().pluginManager.callEvent(ProxyPingEvent(this, r, callback))
        }
        pingBack.done(
            ServerPing(
                ServerPing.Protocol("MoeLimbo", (v ?: Version.UNDEFINED).number),
                ServerPing.Players(
                    (listenerInfo.maxPlayers), LimboLoader.connections.size + BungeeCord.getInstance().onlineCount, null),
                listenerInfo.motd,
                BungeeCord.getInstance().config.faviconObject
            ), null
        )
        return PingConverter(pingResult)
    }


    companion object {
        val listenerInfo: ListenerInfo = BungeeCord.getInstance().config.listeners.iterator().next()
    }


}