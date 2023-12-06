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
import io.netty.channel.ChannelPipeline
import net.md_5.bungee.BungeeCord
import net.md_5.bungee.api.connection.PendingConnection
import net.md_5.bungee.connection.InitialHandler
import net.md_5.bungee.netty.ChannelWrapper
import net.md_5.bungee.protocol.DefinedPacket
import java.lang.reflect.Field
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.util.concurrent.TimeUnit

@Suppress("UNUSED", "MemberVisibilityCanBePrivate")
class ConnectionUtil(val connection: PendingConnection) {
    private val bungee = BungeeCord.getInstance()

    val pipeline: ChannelPipeline? =
        (try { PlayerChannelRecord.getChannelHandler(bungee.getPlayer(connection.uniqueId))?.pipeline() } catch (npe: NullPointerException) { null } )
            ?: initChannelWrapper()?.handle?.pipeline()

    val version get() = connection.version
    val isConnected get() = connection.isConnected
    val socketAddress: SocketAddress get() = connection.socketAddress
    val inetSocketAddress get() = socketAddress as InetSocketAddress
    val inetAddress: InetAddress get() = inetSocketAddress.address
    val virtualHost: InetSocketAddress? get() = connection.virtualHost

    fun close() { pipeline?.close() ?: connection.disconnect() }

    fun writePacket(packet: DefinedPacket) { connection.unsafe().sendPacket(packet) }

    fun writePacket(packet: Any) { pipeline!!.write(packet) }

    private fun initChannelWrapper(): ChannelWrapper? {

        val initialHandler = connection as InitialHandler
        var field: Field? = null
        val result =  try {
            field = initialHandler.javaClass.getDeclaredField("ch")
            field!!.isAccessible=true
            field[initialHandler] as ChannelWrapper
        } catch (_: NoSuchFieldException) { null }
        finally { if (field != null) { field.isAccessible=false } }
        result?.let { wrapperCache.put(initialHandler, it) }
        return result
    }

    companion object {
        val wrapperCache = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .build<InitialHandler, ChannelWrapper>()
    }
}