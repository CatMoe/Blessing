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

import catmoe.fallencrystal.moefilter.MoeFilterBungee
import catmoe.fallencrystal.moefilter.api.proxy.ProxyCache
import catmoe.fallencrystal.moefilter.common.check.AbstractCheck
import catmoe.fallencrystal.moefilter.common.check.info.CheckInfo
import catmoe.fallencrystal.moefilter.common.check.info.impl.Address
import catmoe.fallencrystal.moefilter.common.check.proxy.ProxyChecker
import catmoe.fallencrystal.moefilter.common.check.proxy.type.ProxyResultType
import catmoe.fallencrystal.moefilter.common.firewall.Firewall
import catmoe.fallencrystal.moefilter.common.firewall.Throttler
import catmoe.fallencrystal.moefilter.util.plugin.util.Scheduler
import java.net.InetAddress

class ProxyCheck : AbstractCheck() {

    private val schedule = Scheduler(MoeFilterBungee.instance)

    override fun increase(info: CheckInfo): Boolean {
        val inetAddress = (info as Address).address.address
        val result = ProxyCache.getProxy(inetAddress)
        if (result == null) { apiCheck(inetAddress); return false }
        if (result.type == ProxyResultType.INTERNAL) { Firewall.addAddress(inetAddress) }
        else if (Throttler.isThrottled(inetAddress)) { Firewall.addAddress(inetAddress) }
        return true
    }

    private fun apiCheck(address: InetAddress) { schedule.runAsync { ProxyChecker.check(address) } }
}