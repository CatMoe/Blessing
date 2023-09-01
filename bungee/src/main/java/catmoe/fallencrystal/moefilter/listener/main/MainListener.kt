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

package catmoe.fallencrystal.moefilter.listener.main

import catmoe.fallencrystal.moefilter.check.info.impl.Address
import catmoe.fallencrystal.moefilter.check.info.impl.Pinging
import catmoe.fallencrystal.moefilter.common.check.misc.CountryCheck
import catmoe.fallencrystal.moefilter.common.check.misc.DomainCheck
import catmoe.fallencrystal.moefilter.common.check.misc.ProxyCheck
import catmoe.fallencrystal.moefilter.common.check.mixed.MixedCheck
import catmoe.fallencrystal.moefilter.common.counter.ConnectionCounter
import catmoe.fallencrystal.moefilter.common.firewall.Firewall
import catmoe.fallencrystal.moefilter.common.firewall.Throttler
import catmoe.fallencrystal.moefilter.network.bungee.handler.PacketHandler
import catmoe.fallencrystal.moefilter.network.bungee.handler.TimeoutHandler
import catmoe.fallencrystal.moefilter.network.bungee.pipeline.IPipeline
import catmoe.fallencrystal.moefilter.network.bungee.pipeline.MoeChannelHandler
import catmoe.fallencrystal.moefilter.network.bungee.pipeline.geyser.GeyserInitializer
import catmoe.fallencrystal.moefilter.network.bungee.util.bconnection.ConnectionUtil
import catmoe.fallencrystal.moefilter.network.common.kick.DisconnectType
import catmoe.fallencrystal.moefilter.network.common.kick.FastDisconnect
import com.github.benmanes.caffeine.cache.Caffeine
import net.md_5.bungee.BungeeCord
import net.md_5.bungee.api.connection.PendingConnection
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.netty.PipelineUtils
import net.md_5.bungee.protocol.packet.Handshake
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.util.concurrent.CompletableFuture

object MainListener {

    var incomingListener: Listener? = null

    // 在握手之前只能建立一个连接
    private val connectionCache = Caffeine.newBuilder().build<InetAddress, Boolean>()

    private val bungee = BungeeCord.getInstance()

    private var isProxyProtocol = false

    init { isProxyProtocol=bungee.config.listeners.iterator().next().isProxyProtocol }

    fun initConnection(address: SocketAddress): Boolean {
        if (isProxyProtocol) return false
        val inetAddress = (address as InetSocketAddress).address
        // Don't firewall them.
        if (connectionCache.getIfPresent(inetAddress) == true) { return true } else { connectionCache.put(inetAddress, true) }
        if (!isProxyProtocol) { ConnectionCounter.increase(inetAddress) }
        return Firewall.isFirewalled(inetAddress) || Throttler.increase(inetAddress)
    }

    fun onHandshake(handshake: Handshake, pc: PendingConnection) {
        val connection = ConnectionUtil(pc)
        val inetAddress = connection.inetAddress
        if (isProxyProtocol) { ConnectionCounter.increase(inetAddress) }
        connectionCache.invalidate(inetAddress)
        if (Firewall.isFirewalled(inetAddress)) { connection.close(); return }
        if (Throttler.isThrottled(inetAddress)) { connection.close() }
        val packetHandler = PacketHandler()
        packetHandler.inetSocketAddress=connection.inetSocketAddress
        packetHandler.protocol.set(handshake.protocolVersion)
        // Use PendingConnection.version insteadof Handshake.protocolVersion.

        // Firewall who connected after an instant disconnected.

        CompletableFuture.runAsync {
            if (!connection.isConnected && !packetHandler.cancelled.get() && packetHandler.isAvailable.get()) { addFirewall(
                connection,
                true
            ) }
        }

        when (handshake.requestedProtocol) {
            1 -> { MixedCheck.increase(Pinging(inetAddress, packetHandler.protocol.get())) }
            2 -> {
                val info = Address(connection.inetSocketAddress, connection.virtualHost)
                if (DomainCheck.instance.increase(info)) { kick(connection, DisconnectType.INVALID_HOST); return }
                if (CountryCheck().increase(info)) { kick(connection, DisconnectType.COUNTRY); return }
                if (ProxyCheck().increase(info)) { kick(connection, DisconnectType.PROXY); return }
            }
            // That is impossible
            else -> { connection.close(); addFirewall(connection, false) }
        }

        /*
        Prevent too many connections from being established from a single IP
        Disconnect those connections that were not disconnected during the InitConnect phase.

        This protection is also effective in preventing some BungeeCord forks they're disabling
        ClientConnectEvent or InitConnectionEvent (Waterfall fork?)
         */
        // 1 = Ping  2 = Join  else = illegal connection.
        val method = handshake.requestedProtocol
        if (method > 2 || method < 1) { connection.close(); Firewall.addAddress(inetAddress); return }

        if (connection.isConnected) {
            val pipeline = connection.pipeline ?: return
            if (GeyserInitializer.isGeyser(pipeline.channel())) return
            pipeline.replace(PipelineUtils.TIMEOUT_HANDLER, PipelineUtils.TIMEOUT_HANDLER, TimeoutHandler(BungeeCord.getInstance().getConfig().timeout.toLong()))
            pipeline.addBefore(PipelineUtils.BOSS_HANDLER, IPipeline.PACKET_INTERCEPTOR, packetHandler)
            pipeline.addLast(IPipeline.LAST_PACKET_INTERCEPTOR, MoeChannelHandler.EXCEPTION_HANDLER)
        } else { if (method != 1) { Firewall.addAddressTemp(inetAddress) } }
    }

    private fun kick(connection: ConnectionUtil, type: DisconnectType) {
        FastDisconnect.disconnect(connection, type)
    }

    private fun addFirewall(connection: ConnectionUtil, temp: Boolean) {
        val address = connection.inetAddress
        if (temp) Firewall.addAddressTemp(address) else Firewall.addAddress(address)
        connection.close()
    }
}