package catmoe.fallencrystal.moefilter.common.check.checks.pingjoin

import catmoe.fallencrystal.moefilter.common.check.BlockedType
import catmoe.fallencrystal.moefilter.common.check.EnableType
import catmoe.fallencrystal.moefilter.common.check.ICheck
import catmoe.fallencrystal.moefilter.common.config.ObjectConfig

class PingJoinCheck : ICheck {
    override fun isDenied(address: String): BlockedType {
        return if (PingJoinCache.isPinged(address)) { BlockedType.IGNORE
        } else { PingJoinCache.setPinged(address); BlockedType.BLOCKED_PING_JOIN }
    }

    override fun activeType(): EnableType { return try {(ObjectConfig.getConfig().getAnyRef("check-type.JOIN-PING") as EnableType)} catch (ex: Exception) { EnableType.ALWAYS } }
}