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

package catmoe.fallencrystal.moefilter.network.bungee.util.bconnection

import catmoe.fallencrystal.moefilter.common.config.LocalConfig
import catmoe.fallencrystal.moefilter.network.bungee.util.PipelineUtil
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

@Suppress("UNUSED", "MemberVisibilityCanBePrivate")
class ConnectionUtil(val connection: PendingConnection) {
    private val bungee = BungeeCord.getInstance()

    private val pipeline: ChannelPipeline? = (try { PipelineUtil.getChannelHandler(bungee.getPlayer(connection.uniqueId))?.pipeline() } catch (npe: NullPointerException) { null } ) ?: initChannelWrapper()?.handle?.pipeline()

    fun from(): String { return connection.virtualHost.hostString }

    fun getVersion(): Int { return connection.version }

    fun isConnected(): Boolean { return connection.isConnected }

    fun socketAddress(): SocketAddress { return connection.socketAddress }

    fun inetSocketAddress(): InetSocketAddress { return socketAddress() as InetSocketAddress }

    fun virtualHost(): InetSocketAddress { return connection.virtualHost }

    fun inetAddress(): InetAddress { return inetSocketAddress().address }

    fun close() { pipeline?.close() ?: connection.disconnect() }

    fun writePacket(packet: DefinedPacket) { connection.unsafe().sendPacket(packet) }

    fun writePacket(packet: Any) { pipeline!!.write(packet) }

    fun getPipeline(): ChannelPipeline? { return pipeline }

    private fun initChannelWrapper(): ChannelWrapper? {
        val initialHandler = connection as InitialHandler
        var field: Field? = null
        val debug = LocalConfig.getConfig().getBoolean("debug")
        return try {
            field = initialHandler.javaClass.getDeclaredField("ch")
            field!!.isAccessible=true
            field[initialHandler] as ChannelWrapper
        } catch (exception: Exception) { if (debug) exception.printStackTrace(); null
        } finally { if (field != null) { field.isAccessible=false } }
    }
}