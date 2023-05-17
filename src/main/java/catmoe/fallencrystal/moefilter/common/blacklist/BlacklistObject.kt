package catmoe.fallencrystal.moefilter.common.blacklist

import com.github.benmanes.caffeine.cache.Caffeine

object BlacklistObject {
    private val cache = Caffeine.newBuilder().build<String, BlacklistProfile>()

    fun setBlacklist(address: String, profile: BlacklistProfile) { cache.put(address, profile) }

    fun getBlacklist(address: String): BlacklistProfile? { return cache.getIfPresent(address) }

    fun profileBuilder(address: String, reason: BlacklistReason): BlacklistProfile { return BlacklistProfile(address, reason.reason) }
    fun profileBuilder(address: String, reason: BlacklistReason, name: String): BlacklistProfile { return BlacklistProfile(address, reason.reason, name) }
}