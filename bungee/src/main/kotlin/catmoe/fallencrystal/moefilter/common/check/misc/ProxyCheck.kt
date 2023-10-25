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

package catmoe.fallencrystal.moefilter.common.check.misc

import catmoe.fallencrystal.moefilter.api.proxy.ProxyCache
import catmoe.fallencrystal.moefilter.check.AbstractCheck
import catmoe.fallencrystal.moefilter.check.info.CheckInfo
import catmoe.fallencrystal.moefilter.check.info.impl.Address
import catmoe.fallencrystal.moefilter.common.check.proxy.type.ProxyResultType
import catmoe.fallencrystal.moefilter.common.firewall.Firewall
import catmoe.fallencrystal.moefilter.common.firewall.Throttler
import catmoe.fallencrystal.moefilter.util.plugin.util.Scheduler

class ProxyCheck : AbstractCheck() {

    private val schedule = Scheduler.getDefault()

    override fun increase(info: CheckInfo): Boolean {
        val inetAddress = (info as Address).address.address
        val result = ProxyCache.getProxy(inetAddress) ?: /* apiCheck(inetAddress);  */return false
        if (result.type == ProxyResultType.INTERNAL) { Firewall.addAddress(inetAddress) }
        else if (Throttler.isThrottled(inetAddress)) { Firewall.addAddress(inetAddress) }
        return true
    }

    //private fun apiCheck(address: InetAddress) { schedule.runAsync { ProxyChecker.check(address) } }
}