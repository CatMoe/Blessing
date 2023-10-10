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
import catmoe.fallencrystal.moefilter.common.counter.ConnectionStatistics
import catmoe.fallencrystal.moefilter.common.counter.type.BlockType
import catmoe.fallencrystal.moefilter.common.firewall.Firewall
import catmoe.fallencrystal.moefilter.common.firewall.Throttler
import catmoe.fallencrystal.moefilter.network.bungee.handler.BungeePacketHandler
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
        if (!isProxyProtocol) { ConnectionStatistics.increase(inetAddress) }
        val result = Firewall.isFirewalled(inetAddress) || Throttler.increase(inetAddress)
        if (result) ConnectionStatistics.countBlocked(BlockType.FIREWALL)
        return result
    }

    fun onHandshake(handshake: Handshake, pc: PendingConnection) {
        val connection = ConnectionUtil(pc)
        val inetAddress = connection.inetAddress
        if (isProxyProtocol) { ConnectionStatistics.increase(inetAddress) }
        connectionCache.invalidate(inetAddress)
        val result = Firewall.isFirewalled(inetAddress) || Throttler.increase(inetAddress)
        if (result) { connection.close(); ConnectionStatistics.countBlocked(BlockType.FIREWALL); return }
        val packetHandler = BungeePacketHandler()
        val method = handshake.requestedProtocol
        packetHandler.inetSocketAddress=connection.inetSocketAddress
        packetHandler.protocol.set(handshake.protocolVersion)
        // Use PendingConnection.version insteadof Handshake.protocolVersion.

        // 1 = Ping  2 = Join  else = illegal connection.
        when (method) {
            1 -> MixedCheck.increase(Pinging(inetAddress, packetHandler.protocol.get()))
            2 -> { if (checkHandshake(Address(connection.inetSocketAddress, connection.virtualHost), connection)) return }
            // That is impossible
            else -> {
                connection.close()
                Firewall.addAddress(inetAddress)
            }
        }

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

    private fun checkHandshake(info: Address, connection: ConnectionUtil): Boolean {
        if (DomainCheck.instance.increase(info)) { kick(connection, DisconnectType.INVALID_HOST); return true }
        if (CountryCheck().increase(info)) { kick(connection, DisconnectType.COUNTRY); return true }
        if (ProxyCheck().increase(info)) { kick(connection, DisconnectType.PROXY); return true }
        return false
    }
}