package catmoe.fallencrystal.moefilter.listener

import catmoe.fallencrystal.moefilter.common.check.checks.pingjoin.PingJoinCache
import net.md_5.bungee.api.event.ProxyPingEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import java.net.InetSocketAddress

class PingListener : Listener {
    @EventHandler
    fun onPing(event: ProxyPingEvent) {
        val address = (event.connection.socketAddress as InetSocketAddress).address.hostAddress
        PingJoinCache.setPinged(address)
    }
}