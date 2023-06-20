package catmoe.fallencrystal.moefilter.network.bungee.util

import catmoe.fallencrystal.moefilter.common.config.ObjectConfig
import catmoe.fallencrystal.moefilter.listener.firewall.FirewallCache
import io.netty.channel.Channel
import java.io.IOException
import java.net.InetSocketAddress

object ExceptionCatcher {
    val debug = ObjectConfig.getConfig().getBoolean("debug")
    @JvmStatic
    fun handle(channel: Channel, cause: Throwable) {
        channel.close()
        if (cause is IOException) return
        if (debug) { cause.printStackTrace() }
        FirewallCache.addAddressTemp((channel.remoteAddress() as InetSocketAddress).address, true)
        // FirewallCache.addAddress((channel.remoteAddress() as InetSocketAddress).address, true)
    }
}