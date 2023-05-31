package catmoe.fallencrystal.moefilter.listener.firewall.listener.waterfall

import catmoe.fallencrystal.moefilter.common.config.ObjectConfig
import catmoe.fallencrystal.moefilter.listener.firewall.FirewallCache
import catmoe.fallencrystal.moefilter.util.message.MessageUtil
import net.md_5.bungee.BungeeCord
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import java.net.InetSocketAddress

class IncomingListener : Listener {

    private val throttle = BungeeCord.getInstance().connectionThrottle

    @EventHandler
    fun onIncomingConnect(event: io.github.waterfallmc.waterfall.event.ConnectionInitEvent) {
        val socketAddress = event.remoteSocketAddress
        val inetAddress = (socketAddress as InetSocketAddress).address
        throttle.unthrottle(socketAddress)
        if (FirewallCache.isFirewalled(inetAddress)) { event.isCancelled = true }
    }

    @EventHandler
    fun onException(event: io.github.waterfallmc.waterfall.event.ProxyExceptionEvent) {
        val exception = event.exception
        if (ObjectConfig.getConfig().getBoolean("debug")) { MessageUtil.logWarn("A exception occurred."); MessageUtil.logWarn(exception.toString()); }
    }
}