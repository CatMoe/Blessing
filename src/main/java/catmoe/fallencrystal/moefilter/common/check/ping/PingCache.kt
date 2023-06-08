package catmoe.fallencrystal.moefilter.common.check.ping

import com.github.benmanes.caffeine.cache.Caffeine
import java.net.InetAddress
import java.util.concurrent.TimeUnit

object PingCache {
    // Address, Protocol version
    private val cache = Caffeine.newBuilder().expireAfterWrite(10, TimeUnit.SECONDS).build<InetAddress, Int>()

    fun increase(address: InetAddress, protocol: Int) { cache.put(address, protocol) }

    fun getProtocol(address: InetAddress): Int? { return cache.getIfPresent(address) }
}