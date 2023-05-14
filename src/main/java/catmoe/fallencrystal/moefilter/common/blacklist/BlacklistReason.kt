package catmoe.fallencrystal.moefilter.common.blacklist

import catmoe.fallencrystal.moefilter.common.config.ObjectConfig

/*
  Copy and edited from AkaneField - Author: FallenCrystal
  https://github.com/CatMoe/AkaneField/blob/main/src/main/java/catmoe/fallencrystal/akanefield/common/objects/profile/BlackListReason.kt
 */
enum class BlacklistReason(@JvmField val reason: String) {
    ADMIN(ObjectConfig.getMessage().getString("blacklist-reason.ADMIN")),
    PROXY(ObjectConfig.getMessage().getString("blacklist-reason.PROXY")),
    PING_LIMIT(ObjectConfig.getMessage().getString("blacklist-reason.PING-LIMIT")),
    JOIN_LIMIT(ObjectConfig.getMessage().getString("blacklist-reason.JOIN-LIMIT")),
    ALTS(ObjectConfig.getMessage().getString("blacklist-reason.ALTS")),
    UNKNOWN_PROTOCOL("") // that should be client join/ping server version when they protocol is invalid.
}