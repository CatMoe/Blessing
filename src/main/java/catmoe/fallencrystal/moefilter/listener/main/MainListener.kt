package catmoe.fallencrystal.moefilter.listener.main

import catmoe.fallencrystal.moefilter.common.utils.counter.ConnectionCounter
import catmoe.fallencrystal.moefilter.listener.firewall.FirewallCache
import net.md_5.bungee.api.connection.PendingConnection
import net.md_5.bungee.api.event.PreLoginEvent
import net.md_5.bungee.protocol.packet.Handshake
import java.net.InetSocketAddress
import java.net.SocketAddress

object MainListener {

    fun initConnection(address: SocketAddress): Boolean {
        val inetAddress = (address as InetSocketAddress).address
        ConnectionCounter.increase(inetAddress)
        return FirewallCache.isFirewalled(inetAddress)
    }

    fun onHandshake(handshake: Handshake, pc: PendingConnection) {
        val protocol = pc.version
        val inetAddress = (pc.socketAddress as InetSocketAddress).address
        // 1 = Ping  2 = Join
        val method = handshake.requestedProtocol
        if (method > 2 || method < 1) { pc.disconnect(); FirewallCache.addAddress(inetAddress, true); return }
        // EndMinecraftPlus Bot(Join+Ping) or PingFlood protocol always is 5. i think there nobody still using 1.7.5-1.7.10 clients
        if (method == 1 && protocol == 5) { pc.disconnect(); FirewallCache.addAddress(inetAddress, true); return }
    }

    fun onLogin(event: PreLoginEvent) { TODO() }
}