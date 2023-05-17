package catmoe.fallencrystal.moefilter.common.check

import catmoe.fallencrystal.moefilter.common.check.checks.firstjoin.FirstJoinCheck
import catmoe.fallencrystal.moefilter.common.check.checks.pingjoin.PingJoinCheck

object CheckManager {
    private val checks: MutableList<ICheck> = ArrayList()

    fun registerCheck(c: ICheck) {
        if (checks.contains(c)) throw ConcurrentModificationException("$c is already registered!")
        checks.add(c)
    }

    fun unregisterCheck(c: ICheck) {
        if (!checks.contains(c)) throw NullPointerException("$c is not registered!")
        checks.remove(c)
    }

    fun shouldBlocked(address: String): BlockedType {
        for (it in checks) {
            val check = it.isDenied(address)
            if (check != BlockedType.IGNORE) return check
        }
        return BlockedType.IGNORE
    }

    fun registerCommonCheck() {
        registerCheck(FirstJoinCheck())
        registerCheck(PingJoinCheck())
    }
}