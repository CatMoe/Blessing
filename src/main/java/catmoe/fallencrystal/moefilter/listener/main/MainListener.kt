package catmoe.fallencrystal.moefilter.listener.main

import catmoe.fallencrystal.moefilter.common.utils.counter.ConnectionCounter
import catmoe.fallencrystal.moefilter.listener.firewall.FirewallCache
import catmoe.fallencrystal.moefilter.util.message.MessageUtil
import com.github.benmanes.caffeine.cache.Caffeine
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.connection.PendingConnection
import net.md_5.bungee.api.event.PreLoginEvent
import net.md_5.bungee.protocol.packet.Handshake
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.util.concurrent.TimeUnit

object MainListener {

    // InetAddress, Protocol
    private val protocolCache = Caffeine.newBuilder().expireAfterWrite(5, TimeUnit.SECONDS).build<InetAddress, Int>()

    private var useLegacyDisconnect = false

    fun initConnection(address: SocketAddress): Boolean {
        val inetAddress = (address as InetSocketAddress).address
        ConnectionCounter.increase(inetAddress)
        return FirewallCache.isFirewalled(inetAddress)
    }

    fun onHandshake(handshake: Handshake, pc: PendingConnection) {
        val protocol = pc.version
        val inetAddress = (pc.socketAddress as InetSocketAddress).address
        if (FirewallCache.isFirewalled(inetAddress)) { pc.disconnect(); if (useLegacyDisconnect) { MessageUtil.logWarn("[MoeFilter] [AntiBot] initConnect are modified. use legacy handshake disconnect.") } }
        // 1 = Ping  2 = Join
        val method = handshake.requestedProtocol
        if (method > 2 || method < 1) { pc.disconnect(); FirewallCache.addAddress(inetAddress, true); return }
        // EndMinecraftPlus Bot(Join+Ping) or PingFlood protocol always is 5. i think there nobody still using 1.7.5-1.7.10 clients
        if (method == 1 && protocol == 5) { pc.disconnect(); FirewallCache.addAddress(inetAddress, true); return }
        val cachedProtocol = protocolCache.getIfPresent(inetAddress)
        // check they protocol. if they changed client protocol in 5 sec. they will be blacklisted.
        if (cachedProtocol != null && cachedProtocol != protocol) { pc.disconnect(); FirewallCache.addAddressTemp(inetAddress, true); return }
        protocolCache.put(inetAddress, protocol)
    }

    fun onLogin(event: PreLoginEvent) {
        val inetAddress = (event.connection.socketAddress as InetSocketAddress).address
        if (event.connection.name.contains("MCSTORM")) { FirewallCache.addAddress(inetAddress, true); event.isCancelled=true; event.setCancelReason(TextComponent("")) }
    }
}