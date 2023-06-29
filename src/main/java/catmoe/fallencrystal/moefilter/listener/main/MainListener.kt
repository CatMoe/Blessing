package catmoe.fallencrystal.moefilter.listener.main

import catmoe.fallencrystal.moefilter.api.proxy.ProxyCache
import catmoe.fallencrystal.moefilter.common.check.info.impl.Pinging
import catmoe.fallencrystal.moefilter.common.check.mixed.MixedCheck
import catmoe.fallencrystal.moefilter.common.utils.counter.ConnectionCounter
import catmoe.fallencrystal.moefilter.common.whitelist.WhitelistObject
import catmoe.fallencrystal.moefilter.listener.firewall.FirewallCache
import catmoe.fallencrystal.moefilter.listener.firewall.Throttler
import catmoe.fallencrystal.moefilter.network.bungee.handler.PacketHandler
import catmoe.fallencrystal.moefilter.network.bungee.handler.TimeoutHandler
import catmoe.fallencrystal.moefilter.network.bungee.pipeline.IPipeline
import catmoe.fallencrystal.moefilter.network.bungee.pipeline.MoeChannelHandler
import catmoe.fallencrystal.moefilter.network.bungee.util.bconnection.ConnectionUtil
import net.md_5.bungee.BungeeCord
import net.md_5.bungee.api.connection.PendingConnection
import net.md_5.bungee.netty.PipelineUtils
import net.md_5.bungee.protocol.packet.Handshake
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.util.concurrent.CompletableFuture

object MainListener {

    fun initConnection(address: SocketAddress): Boolean {
        val inetAddress = (address as InetSocketAddress).address
        ConnectionCounter.increase(inetAddress)
        return FirewallCache.isFirewalled(inetAddress) || Throttler.increase(inetAddress) && !WhitelistObject.isWhitelist(inetAddress)
    }

    fun onHandshake(handshake: Handshake, pc: PendingConnection) {
        val connection = ConnectionUtil(pc)
        // Use PendingConnection.version insteadof Handshake.protocolVersion.
        val inetAddress = connection.inetAddress()

        // Firewall who connected after an instant disconnected.
        CompletableFuture.runAsync { if (!pc.isConnected) { FirewallCache.addAddressTemp(connection.inetAddress(), true) } else if (handshake.requestedProtocol == 1) { MixedCheck.increase(Pinging(inetAddress)) } }

        if (WhitelistObject.isWhitelist(inetAddress)) return

        if (ProxyCache.isProxy(inetAddress)) { connection.close(); addFirewall(inetAddress, pc, false) }

        /*
        Prevent too many connections from being established from a single IP
        Disconnect those connections that were not disconnected during the InitConnect phase.

        This protection is also effective in preventing some BungeeCord forks they're disabling
        ClientConnectEvent or InitConnectionEvent (Waterfall fork?)
         */
        if (FirewallCache.isFirewalled(inetAddress)) { connection.close(); return }
        // 1 = Ping  2 = Join  else = illegal connection.
        val method = handshake.requestedProtocol
        if (method > 2 || method < 1) { connection.close(); FirewallCache.addAddress(inetAddress, false); return }

        if (connection.isConnected()) {
            val pipeline = connection.getPipeline() ?: return
            if (pipeline.channel().parent() != null && pipeline.channel().parent().javaClass.canonicalName.startsWith("org.geysermc.geyser")) return

            pipeline.replace(PipelineUtils.TIMEOUT_HANDLER, PipelineUtils.TIMEOUT_HANDLER, TimeoutHandler(BungeeCord.getInstance().getConfig().timeout.toLong()))
            pipeline.addBefore(PipelineUtils.BOSS_HANDLER, IPipeline.PACKET_INTERCEPTOR, PacketHandler())
            pipeline.addLast(IPipeline.LAST_PACKET_INTERCEPTOR, MoeChannelHandler.EXCEPTION_HANDLER)
        } else { FirewallCache.addAddressTemp(inetAddress, true) }
    }

    private fun addFirewall(inetAddress: InetAddress, pc: PendingConnection, temp: Boolean) {
        if (temp) FirewallCache.addAddressTemp(inetAddress, true) else FirewallCache.addAddress(inetAddress, true)
        pc.disconnect()
    }
}