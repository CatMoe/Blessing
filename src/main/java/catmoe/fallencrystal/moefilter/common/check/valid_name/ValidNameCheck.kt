package catmoe.fallencrystal.moefilter.common.check.valid_name

import catmoe.fallencrystal.moefilter.common.check.AbstractCheck
import catmoe.fallencrystal.moefilter.common.check.info.CheckInfo
import catmoe.fallencrystal.moefilter.common.check.info.impl.Joining
import catmoe.fallencrystal.moefilter.common.config.ObjectConfig
import catmoe.fallencrystal.moefilter.listener.firewall.FirewallCache
import catmoe.fallencrystal.moefilter.listener.firewall.Throttler

object ValidNameCheck : AbstractCheck() {

    private var regexPattern = "(?i)^(?!.*(?:mcstorm|mcdown|bot))[A-Za-z0-9_]{3,15}$"

    override fun increase(info: CheckInfo): Boolean {
        val result = Regex(regexPattern).matches((info as Joining).username)
        if (result && Throttler.isThrottled(info.address)) { FirewallCache.addAddress(info.address, true) }
        return result
    }

    fun init() { regexPattern=ObjectConfig.getAntibot().getString("general.valid-regex") }
}