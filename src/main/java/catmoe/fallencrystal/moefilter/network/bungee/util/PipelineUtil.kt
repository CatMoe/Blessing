package catmoe.fallencrystal.moefilter.network.bungee.util

import com.github.benmanes.caffeine.cache.Caffeine
import io.netty.channel.Channel
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.connection.ProxiedPlayer

object PipelineUtil {
    private val cache = Caffeine.newBuilder().build<Channel, String>()


    private val bungee = ProxyServer.getInstance()

    fun getPlayer(channel: Channel): ProxiedPlayer? { return bungee.getPlayer(cache.getIfPresent(channel)) }

    fun putChannel(channel: Channel, name: String) { cache.put(channel, name) }
}