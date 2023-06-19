package catmoe.fallencrystal.moefilter.listener.main

import catmoe.fallencrystal.moefilter.api.proxy.ProxyCache
import catmoe.fallencrystal.moefilter.common.check.ping_and_join.PingAndJoin.increaseJoin
import catmoe.fallencrystal.moefilter.common.check.ping_and_join.PingAndJoin.increasePing
import catmoe.fallencrystal.moefilter.common.check.ping_and_join.PingAndJoin.invalidateJoinCache
import catmoe.fallencrystal.moefilter.common.check.ping_and_join.PingAndJoin.invalidatePingCache
import catmoe.fallencrystal.moefilter.common.utils.counter.ConnectionCounter
import catmoe.fallencrystal.moefilter.common.whitelist.WhitelistObject
import catmoe.fallencrystal.moefilter.listener.firewall.FirewallCache
import catmoe.fallencrystal.moefilter.listener.firewall.Throttler
import catmoe.fallencrystal.moefilter.util.message.MessageUtil
import net.md_5.bungee.UserConnection
import net.md_5.bungee.api.connection.PendingConnection
import net.md_5.bungee.api.event.PostLoginEvent
import net.md_5.bungee.api.event.PreLoginEvent
import net.md_5.bungee.protocol.packet.Handshake
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.SocketAddress

object MainListener {

    fun initConnection(address: SocketAddress): Boolean {
        val inetAddress = (address as InetSocketAddress).address
        ConnectionCounter.increase(inetAddress)
        return FirewallCache.isFirewalled(inetAddress) || Throttler.increase(inetAddress) && !WhitelistObject.isWhitelist(inetAddress)
    }

    fun onHandshake(handshake: Handshake, pc: PendingConnection) {
        // Use PendingConnection.version insteadof Handshake.protocolVersion.
        val protocol = pc.version
        val inetAddress = (pc.socketAddress as InetSocketAddress).address
        if (WhitelistObject.isWhitelist(inetAddress)) return

        if (ProxyCache.isProxy(inetAddress)) { pc.disconnect(); addFirewall(inetAddress, pc, false) }

        /*
        Prevent too many connections from being established from a single IP
        Disconnect those connections that were not cut during the InitConnect phase.

        This protection is also effective in preventing some BungeeCord forks they're disabling
        ClientConnectEvent or InitConnectionEvent (Waterfall fork?)
         */
        if (FirewallCache.isFirewalled(inetAddress)) { pc.disconnect(); return }
        // 1 = Ping  2 = Join  else = illegal connection.
        val method = handshake.requestedProtocol
        if (method > 2 || method < 1) { pc.disconnect(); FirewallCache.addAddress(inetAddress, false); return }
        /*
        EndMinecraftPlus Bot(Join+Ping or PingFlood)
        they protocol is always 5. I think there nobody still using 1.7.5-1.7.10 clients.

        Edit: Don't block then on firewall(just disconnect)
        If under EndMinecraftPlus bot attack.
        use change protocol check and ping+join checks
        (they should be blocking. because they ping protocol version of ping is different from the version at the time of joining.)
         */

        // if (method == 1 && protocol == 5) { pc.disconnect(); return }
        if (method == 1) {
            increasePing(inetAddress, protocol)
        }

        if (method == 2) {
            increaseJoin(inetAddress, protocol)
        }

        // cached protocol removed. use PingCache for check protocol. no more bot switches their protocol when joining.
    }

    fun onLogin(event: PreLoginEvent) {
        val inetAddress = (event.connection.socketAddress as InetSocketAddress).address

        if (WhitelistObject.isWhitelist(inetAddress)) return

        // Only mc storm bot name here. I will add config for this as soon as possible.
        if (event.connection.name.contains("MCSTORM")) {
            /*
            Use PendingConnection.disconnect() insteadof event.setCancelReason.
            Invalid name bots don't care disconnect reason, so don't waste bandwidth on sending reason.
            Also, it can help disconnect quickly to avoid waiting calling PreLoginEvent and froze BungeeCord.
             */
            addFirewall(inetAddress, event.connection, false)
            event.isCancelled=true
            /*
            Null = BungeeCord default disconnect message. "Proxy lost connect from server."
            This reason will not be sent to the client. Because we've disconnected on PendingConnection.
             */
            event.setCancelReason(MessageUtil.colorizeTextComponent("Cancelled by MoeFilter."))
        }
    }

    fun postLoginHandler(event: PostLoginEvent) {
        val uc = event.player as UserConnection
    }

    private fun addFirewall(inetAddress: InetAddress, pc: PendingConnection, temp: Boolean) {
        invalidateJoinCache(inetAddress); invalidatePingCache(inetAddress)
        if (temp) FirewallCache.addAddressTemp(inetAddress, true) else FirewallCache.addAddress(inetAddress, true)
        pc.disconnect()
    }
}