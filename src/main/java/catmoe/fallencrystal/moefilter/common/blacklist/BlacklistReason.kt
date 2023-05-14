package catmoe.fallencrystal.moefilter.common.blacklist

import catmoe.fallencrystal.moefilter.common.config.ObjectConfig

/*
  Copy and edited from AkaneField - Author: FallenCrystal
  https://github.com/CatMoe/AkaneField/blob/main/src/main/java/catmoe/fallencrystal/akanefield/common/objects/profile/BlackListReason.kt
 */
enum class BlacklistReason(@JvmField val reason: String) {
    ADMIN(ObjectConfig.getMessage().getString("blacklist-reason.ADMIN")),
    PROXY(ObjectConfig.getMessage().getString("blacklist-reason.PROXY"))
}