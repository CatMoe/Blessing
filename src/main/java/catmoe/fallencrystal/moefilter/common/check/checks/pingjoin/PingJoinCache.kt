package catmoe.fallencrystal.moefilter.common.check.checks.pingjoin

import com.github.benmanes.caffeine.cache.Caffeine

object PingJoinCache {
    private val cache = Caffeine.newBuilder().build<String, Boolean>()

    fun isPinged(address: String): Boolean { return cache.getIfPresent(address) ?: false }

    fun setPinged(address: String) { cache.put(address, true) }
}