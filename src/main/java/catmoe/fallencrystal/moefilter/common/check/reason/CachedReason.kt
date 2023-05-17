package catmoe.fallencrystal.moefilter.common.check.reason

import catmoe.fallencrystal.moefilter.common.config.ObjectConfig
import catmoe.fallencrystal.moefilter.util.message.KickMessage
import com.github.benmanes.caffeine.cache.Caffeine

object CachedReason {
    private val reason = Caffeine.newBuilder().build<KickReason, List<String>>()
    private val reasonToString = mapOf(
        KickReason.Blacklisted to ObjectConfig.getMessage().getStringList("kick.blacklist"),
        KickReason.FirstJoin to ObjectConfig.getMessage().getStringList("kick.first-join"),
        KickReason.PingJoin to ObjectConfig.getMessage().getStringList("kick.ping-join")
    )

    fun cacheReason() {
        reason.invalidateAll()
        for ((objects, reasons) in reasonToString) {
            val reasonPlaceholder = KickMessage.replacePlaceholders(reasons)
            reason.put(objects, reasonPlaceholder)
        }
    }

    fun getReason(objects: KickReason): List<String>? { return reason.getIfPresent(objects) }
}