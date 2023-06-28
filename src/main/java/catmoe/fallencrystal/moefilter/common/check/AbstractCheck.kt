package catmoe.fallencrystal.moefilter.common.check

import catmoe.fallencrystal.moefilter.common.check.info.CheckInfo

abstract class AbstractCheck {
    open fun increase(info: CheckInfo): Boolean { return true }
}