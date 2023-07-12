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

package catmoe.fallencrystal.moefilter.common.check.misc

import catmoe.fallencrystal.moefilter.common.check.AbstractCheck
import catmoe.fallencrystal.moefilter.common.check.info.CheckInfo
import catmoe.fallencrystal.moefilter.common.check.info.impl.Joining
import catmoe.fallencrystal.moefilter.common.config.LocalConfig
import catmoe.fallencrystal.moefilter.listener.firewall.FirewallCache
import catmoe.fallencrystal.moefilter.listener.firewall.Throttler

class ValidNameCheck : AbstractCheck() {

    private var regexPattern = "(?i)^(?!.*(?:mcstorm|mcdown|bot))[A-Za-z0-9_]{3,16}$"

    init { instance =this }

    override fun increase(info: CheckInfo): Boolean {
        val result = !Regex(regexPattern).matches((info as Joining).username)
        if (result && Throttler.isThrottled(info.address)) { FirewallCache.addAddress(info.address, true) }
        return result
    }

    fun init() { regexPattern=LocalConfig.getAntibot().getString("general.valid-regex") }

    companion object {
        lateinit var instance: ValidNameCheck
            private set
    }
}