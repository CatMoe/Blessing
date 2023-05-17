package catmoe.fallencrystal.moefilter.common.check.checks.firstjoin

import com.github.benmanes.caffeine.cache.Caffeine

object FirstJoinCache {
    private val cache = Caffeine.newBuilder().build<String, Boolean>()

    fun isJoined(address: String): Boolean { return cache.getIfPresent(address) ?: false }

    fun setJoined(address: String) { cache.put(address, true) }
}