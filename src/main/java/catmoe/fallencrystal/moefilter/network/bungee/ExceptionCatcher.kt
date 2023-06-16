package catmoe.fallencrystal.moefilter.network.bungee

import catmoe.fallencrystal.moefilter.listener.firewall.FirewallCache
import io.netty.channel.Channel
import java.io.IOException
import java.net.InetSocketAddress

object ExceptionCatcher {
    @JvmStatic
    fun handle(channel: Channel, cause: Throwable) {
        channel.close()
        if (cause is IOException) return
        FirewallCache.addAddress((channel.remoteAddress() as InetSocketAddress).address, true)
    }
}