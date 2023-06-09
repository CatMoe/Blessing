package catmoe.fallencrystal.moefilter.common.check.ping_and_join

import catmoe.fallencrystal.moefilter.common.check.ping_and_join.CheckType.*
import com.github.benmanes.caffeine.cache.Caffeine
import java.net.InetAddress
import java.util.concurrent.TimeUnit

object PingAndJoin {
    // Address, Protocol version
    private val pingCache = Caffeine.newBuilder().expireAfterWrite(10, TimeUnit.SECONDS).build<InetAddress, Int>()
    private val joinCache = Caffeine.newBuilder().expireAfterWrite(10, TimeUnit.SECONDS).build<InetAddress, Int>()

    private var method: CheckType? = null

    fun check(address: InetAddress, protocol: Int): KickType {
        when (method) {
            PING_AFTER_RECONNECT -> {
                val cachedPing = pingCache.getIfPresent(address)
                val cachedJoin = joinCache.getIfPresent(address)
                if (cachedPing == null || protocol != cachedPing) { joinCache.invalidate(address); pingCache.invalidate(address); return KickType.BEFORE_PING }
                if (cachedJoin == null || protocol != cachedJoin) { joinCache.invalidate(address); return KickType.RECONNECT }
            }
            RECONNECT_AFTER_PING -> {
                val cachedJoin = joinCache.getIfPresent(address)
                if (cachedJoin == null || protocol != cachedJoin) { pingCache.invalidate(address); joinCache.invalidate(address) ; return KickType.RECONNECT }
                val cachedPing = pingCache.getIfPresent(address)
                if (cachedPing == null || protocol != cachedPing) { pingCache.invalidate(address); return KickType.BEFORE_PING }
            }
            ONLY_PING -> {
                val cached = pingCache.getIfPresent(address)
                return if (cached == null || protocol != cached) { pingCache.invalidate(address); KickType.BEFORE_PING } else { KickType.NULL }
            }
            ONLY_RECONNECT -> {
                val cached = joinCache.getIfPresent(address)
                return if (cached == null || protocol != cached) { pingCache.invalidate(address); KickType.RECONNECT } else { KickType.NULL }
            }
            PING_RECONNECT_STABLE -> {
                val cachedPing = pingCache.getIfPresent(address)
                if (cachedPing == null || protocol != cachedPing) { pingCache.invalidate(address); return KickType.BEFORE_PING }
                val cachedJoin = joinCache.getIfPresent(address)
                return if (cachedJoin == null || protocol != cachedJoin) { joinCache.invalidate(address); KickType.RECONNECT } else { KickType.NULL }
            }
            null -> { return KickType.NULL }
        }
        return KickType.NULL
    }

    fun increaseJoin(address: InetAddress, protocol: Int) { joinCache.put(address, protocol) }
    fun increasePing(address: InetAddress, protocol: Int) { pingCache.put(address, protocol) }

    fun getCachedPingProtocol(address: InetAddress): Int? { return pingCache.getIfPresent(address) }
    fun getCachedJoinProtocol(address: InetAddress): Int? { return joinCache.getIfPresent(address) }

    fun setMethod(method: CheckType) { this.method=method }
}