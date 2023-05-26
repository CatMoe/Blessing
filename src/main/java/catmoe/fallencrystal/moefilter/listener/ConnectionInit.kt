package catmoe.fallencrystal.moefilter.listener

import catmoe.fallencrystal.moefilter.api.proxy.ProxyCache
import io.github.waterfallmc.waterfall.event.ConnectionInitEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import java.net.InetSocketAddress

class ConnectionInit : Listener {
    @EventHandler
    fun onConnect(event: ConnectionInitEvent) {
        val address = (event.remoteSocketAddress as? InetSocketAddress)!!.address
        if (ProxyCache.isProxy(address)) { event.isCancelled = true }
    }
}