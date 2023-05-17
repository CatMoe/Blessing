package catmoe.fallencrystal.moefilter.common.check.checks.firstjoin

import catmoe.fallencrystal.moefilter.common.check.BlockedType
import catmoe.fallencrystal.moefilter.common.check.EnableType
import catmoe.fallencrystal.moefilter.common.check.ICheck
import catmoe.fallencrystal.moefilter.common.config.ObjectConfig

class FirstJoinCheck : ICheck {
    override fun isDenied(address: String): BlockedType {
        return if (FirstJoinCache.isJoined(address)) { BlockedType.IGNORE
        } else { FirstJoinCache.setJoined(address); BlockedType.BLOCKED_FIRST_JOIN }
    }

    override fun activeType(): EnableType { return try {(ObjectConfig.getConfig().getAnyRef("check-type.FIRST-JOIN") as EnableType)} catch (ex: Exception) { EnableType.ALWAYS } }
}