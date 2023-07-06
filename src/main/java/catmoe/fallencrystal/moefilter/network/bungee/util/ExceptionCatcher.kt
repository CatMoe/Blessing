package catmoe.fallencrystal.moefilter.network.bungee.util

import catmoe.fallencrystal.moefilter.common.config.LocalConfig
import catmoe.fallencrystal.moefilter.util.message.v2.MessageUtil
import catmoe.fallencrystal.moefilter.listener.firewall.FirewallCache
import catmoe.fallencrystal.moefilter.network.bungee.util.exception.DebugException
import catmoe.fallencrystal.moefilter.network.bungee.util.exception.InvalidHandshakeStatusException
import catmoe.fallencrystal.moefilter.network.bungee.util.exception.InvalidStatusPingException
import com.typesafe.config.ConfigException
import io.netty.channel.Channel
import java.io.IOException
import java.net.InetSocketAddress

object ExceptionCatcher {
    private var debug = false
    @JvmStatic
    fun handle(channel: Channel, cause: Throwable) {
        channel.close()
        if (debug) { cause.printStackTrace() }
        val address = (channel.remoteAddress() as InetSocketAddress).address
        if (cause is IOException) return
        if (cause is DebugException) { cause.printStackTrace(); return }
        if (cause is InvalidStatusPingException || cause is InvalidHandshakeStatusException) { FirewallCache.addAddress(address, true); return }
        if (cause is ConfigException) { MessageUtil.logError("<red>A connection force closed because your config has critical issue"); cause.printStackTrace(); return }
        FirewallCache.addAddressTemp(address, true)
    }

    fun reload() { debug = LocalConfig.getConfig().getBoolean("debug") }
}
