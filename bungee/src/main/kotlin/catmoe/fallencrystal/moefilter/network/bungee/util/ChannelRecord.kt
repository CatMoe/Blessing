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

package catmoe.fallencrystal.moefilter.network.bungee.util

import com.github.benmanes.caffeine.cache.Caffeine
import io.netty.channel.Channel
import net.md_5.bungee.ServerConnector
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.connection.Server
import net.md_5.bungee.connection.InitialHandler

@Suppress("MemberVisibilityCanBePrivate")
object ChannelRecord {
    private val upstream = Caffeine.newBuilder().build<InitialHandler, Channel>()
    private val downstream = Caffeine.newBuilder().build<Server, Channel>()
    private val linkedConnector = mutableMapOf<InitialHandler, Server>()


    private val bungee = ProxyServer.getInstance()

    fun putDownstream(server: Server, handler: InitialHandler, channel: Channel) {
        downstream.put(server, channel)
        linkedConnector[handler] = server
    }
    fun getDownstream(server: Server) = downstream.getIfPresent(server)
    fun getDownstream(handler: InitialHandler) = linkedConnector[handler]?.let { getDownstream(it) }
    fun getUpstream(handler: InitialHandler) = upstream.getIfPresent(handler)
    @Deprecated("Prevent use connector to get InitialHandler")
    fun getUpstream(connector: ServerConnector) = linkedConnector.getKeyByValue(connector)?.let { getUpstream(it) }
    fun putUpstream(handler: InitialHandler, channel: Channel) = upstream.put(handler, channel)

    fun invalidate(handler: InitialHandler) {
        upstream.invalidate(handler)
        linkedConnector[handler]?.let { downstream.invalidate(it) }
        linkedConnector.remove(handler)
    }

    private fun <K, V> Map<K, V>.getKeyByValue(value: V): K? = entries.find { it.value == value }?.key
}
