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

package catmoe.fallencrystal.moefilter.common.check.proxy.ipapi

import catmoe.fallencrystal.moefilter.api.proxy.ProxyCache
import catmoe.fallencrystal.moefilter.common.check.proxy.IProxyChecker
import catmoe.fallencrystal.moefilter.common.check.proxy.type.ProxyResult
import catmoe.fallencrystal.moefilter.common.check.proxy.type.ProxyResultType
import catmoe.fallencrystal.moefilter.common.check.proxy.util.ClientHelper
import catmoe.fallencrystal.moefilter.util.plugin.util.Scheduler
import com.github.benmanes.caffeine.cache.Caffeine
import net.md_5.bungee.api.scheduler.ScheduledTask
import okhttp3.OkHttpClient
import java.net.InetAddress
import java.util.*
import java.util.concurrent.TimeUnit

class IPAPIChecker : IProxyChecker {

    private val checked = Caffeine.newBuilder().build<InetAddress, Boolean>()
    private val queue: Queue<InetAddress> = ArrayDeque()
    private val schedule = Scheduler.getDefault()

    private var scheduleTask: ScheduledTask? = null

    override fun schedule() {
        // 1500 delay. Prevent to reached call limit.
        scheduleTask = schedule.repeatScheduler(1500, 1500 , TimeUnit.MILLISECONDS) {
            if (queue.isNotEmpty()) {
                val address = queue.poll()
                schedule.runAsync { if (check(address)) { ProxyCache.addProxy(ProxyResult(address, ProxyResultType.IP_API)) } }
            }
        }
    }

    override fun stopSchedule() { scheduleTask?.cancel() }

    override fun addAddress(address: InetAddress) { if (checked.getIfPresent(address) != null || queue.contains(address) || ProxyCache.isProxy(address)) return else queue.add(address) }

    override fun check(address: InetAddress?): Boolean {
        if (address == null) return false
        if (address.let { checked.getIfPresent(it) } == true) { return ProxyCache.isProxy(address) }
        // field 147456 = status, proxy
        // I'm actually too lazy to actually parse those xml/json. I just need to use .contains() to try to match them.
        // InetAddress .toString -> /xx.xx.xx.xx. So that actually like http://ip-api.com/xml/127.0.0.1?fields=147456
        val url = "http://ip-api.com/xml$address?fields=147456"
        val helper = ClientHelper(OkHttpClient.Builder(), url)
        helper.setProxy(true)
        try {
            val response = helper.getResponse()
            if (response.isSuccessful) {
                val text = response.body!!.string()
                if (text.contains("<status>success</status>")) { return text.contains("<proxy>true</proxy>") }
                checked.put(address, true)
            }
            response.close()
        } catch (_: Exception) { return false }
        return false
    }

}