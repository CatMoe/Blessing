package catmoe.fallencrystal.moefilter.common.check.checks.firstjoin

import catmoe.fallencrystal.moefilter.common.check.BlockedType
import catmoe.fallencrystal.moefilter.common.check.ICheck

class FirstJoinCheck : ICheck {
    override fun isDenied(address: String): BlockedType {
        return if (FirstJoinCache.isJoined(address)) { BlockedType.IGNORE
        } else { FirstJoinCache.setJoined(address); BlockedType.BLOCKED_FIRST_JOIN }
    }
}