/*
 * Copyright (C) 2023-2023. CatMoe / MoeFilter Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package catmoe.fallencrystal.moefilter.common.check.name.valid

import catmoe.fallencrystal.moefilter.check.AbstractCheck
import catmoe.fallencrystal.moefilter.check.info.CheckInfo
import catmoe.fallencrystal.moefilter.check.info.impl.Joining
import catmoe.fallencrystal.moefilter.common.check.name.valid.AutoFirewallMode.*
import catmoe.fallencrystal.moefilter.common.firewall.Firewall
import catmoe.fallencrystal.moefilter.common.firewall.Throttler
import catmoe.fallencrystal.moefilter.common.state.StateManager
import catmoe.fallencrystal.translation.utils.config.LocalConfig
import catmoe.fallencrystal.translation.utils.config.Reloadable

class ValidNameCheck : AbstractCheck(), Reloadable {

    private var regexPattern = "(?i)^(?!.*(?:mcstorm|mcdown|bot))[A-Za-z0-9_]{3,16}$"

    private var autoFirewallMode = DISABLED

    init { instance =this }

    override fun increase(info: CheckInfo): Boolean {
        val result = !Regex(regexPattern).matches((info as Joining).username)
        val address = info.address
        if (result) {
            when (autoFirewallMode) {
                THROTTLE -> { if (Throttler.isThrottled(address)) { Firewall.addAddressTemp(address) } }
                ALWAYS -> { Firewall.addAddressTemp(address) }
                ATTACK -> { if (StateManager.inAttack.get()) { Firewall.addAddressTemp(address) } }
                DISABLED -> {}
            }
        }
        return result
    }

    override fun reload() {
        this.init()
    }

    fun init() {
        val config = LocalConfig.getAntibot().getConfig("name-check.valid-check")
        regexPattern=config.getString("valid-regex")
        autoFirewallMode=try { AutoFirewallMode.valueOf(config.getAnyRef("firewall-mode").toString()) } catch (_: Exception) { DISABLED }
    }

    companion object {
        lateinit var instance: ValidNameCheck
            private set
    }
}