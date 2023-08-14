/*
 * Copyright 2023. CatMoe / FallenCrystal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package catmoe.fallencrystal.moefilter.common.check.name.valid

import catmoe.fallencrystal.moefilter.common.check.AbstractCheck
import catmoe.fallencrystal.moefilter.common.check.info.CheckInfo
import catmoe.fallencrystal.moefilter.common.check.info.impl.Joining
import catmoe.fallencrystal.moefilter.common.check.name.valid.AutoFirewallMode.*
import catmoe.fallencrystal.moefilter.common.config.LocalConfig
import catmoe.fallencrystal.moefilter.common.firewall.Firewall
import catmoe.fallencrystal.moefilter.common.firewall.Throttler
import catmoe.fallencrystal.moefilter.common.state.StateManager

class ValidNameCheck : AbstractCheck() {

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