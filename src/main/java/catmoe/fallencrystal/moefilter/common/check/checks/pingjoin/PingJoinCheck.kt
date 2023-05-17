package catmoe.fallencrystal.moefilter.common.check.checks.pingjoin

import catmoe.fallencrystal.moefilter.common.check.BlockedType
import catmoe.fallencrystal.moefilter.common.check.ICheck

class PingJoinCheck : ICheck {
    override fun isDenied(address: String): BlockedType {
        return if (PingJoinCache.isPinged(address)) { BlockedType.IGNORE
        } else { PingJoinCache.setPinged(address); BlockedType.BLOCKED_PING_JOIN }
    }
}