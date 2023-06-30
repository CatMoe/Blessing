package catmoe.fallencrystal.moefilter.listener.firewall

import catmoe.fallencrystal.moefilter.common.config.LocalConfig
import com.github.benmanes.caffeine.cache.Caffeine
import java.net.InetAddress
import java.util.concurrent.TimeUnit

object Throttler {
    private val ipCache = Caffeine.newBuilder().expireAfterWrite(1, TimeUnit.SECONDS).build<InetAddress, Int>()

    private var throttle = 3

    fun increase(address: InetAddress): Boolean {
        val count = ipCache.getIfPresent(address) ?: 0
        ipCache.put(address, count + 1)
        return count >= throttle
    }

    fun isThrottled(address: InetAddress): Boolean { return (ipCache.getIfPresent(address) ?: 0) >= throttle }

    fun reload() { this.throttle=LocalConfig.getConfig().getInt("throttle-limit") }
}