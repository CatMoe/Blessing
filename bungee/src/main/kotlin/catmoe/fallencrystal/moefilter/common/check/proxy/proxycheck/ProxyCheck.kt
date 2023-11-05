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

package catmoe.fallencrystal.moefilter.common.check.proxy.proxycheck

import catmoe.fallencrystal.moefilter.api.proxy.ProxyCache
import catmoe.fallencrystal.moefilter.common.check.proxy.IProxyChecker
import catmoe.fallencrystal.moefilter.common.check.proxy.type.ProxyResult
import catmoe.fallencrystal.moefilter.common.check.proxy.type.ProxyResultType
import catmoe.fallencrystal.moefilter.common.check.proxy.util.ClientHelper
import catmoe.fallencrystal.moefilter.util.message.v2.MessageUtil
import catmoe.fallencrystal.moefilter.util.plugin.util.Scheduler
import catmoe.fallencrystal.translation.utils.config.LocalConfig
import com.github.benmanes.caffeine.cache.Caffeine
import net.md_5.bungee.api.scheduler.ScheduledTask
import okhttp3.OkHttpClient
import java.net.InetAddress
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class ProxyCheck : IProxyChecker {
    private val schedule = Scheduler.getDefault()

    private var config = LocalConfig.getProxy().getConfig("proxycheck-io")
    private var dayLimit = config.getInt("limit")
    private var minuteLimit = config.getInt("throttle")
    private val checkCount = AtomicInteger(0)
    private val minuteCount = AtomicInteger(0)
    private val key = config.getString("key")
    private val license: String? = if (key.isEmpty() || key.contains("your-key-here")) null else key
    private val useProxy = !config.getBoolean("direct-response")
    private val checkVPN = config.getBoolean("check-vpn")

    private val queue: Queue<InetAddress> = ArrayDeque()
    private  val checked = Caffeine.newBuilder().build<InetAddress, Boolean>()
    private var checkSchedule: ScheduledTask? = null
    private var allSchedule: MutableCollection<ScheduledTask> = CopyOnWriteArrayList()
    private val queuedLimit: MutableList<InetAddress> = CopyOnWriteArrayList()

    override fun schedule() {
        allSchedule.add(schedule.repeatScheduler(1, 1, TimeUnit.MINUTES) { minuteCount.set(0) })
        allSchedule.add(schedule.repeatScheduler(1, 1, TimeUnit.DAYS) { checkCount.set(0); queue.addAll(queuedLimit) })
        this.check(null)
    }

    override fun stopSchedule() { allSchedule.forEach { it.cancel() } }

    override fun addAddress(address: InetAddress) { if (checked.getIfPresent(address) == null && !ProxyCache.isProxy(address)) { queue.add(address) } }

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun check(empty: InetAddress?): Boolean {
        checkSchedule=schedule.repeatScheduler(200, TimeUnit.MILLISECONDS) {
            val checkCount = this.checkCount.get()
            val minuteCount = this.minuteCount.get()
            if (queue.isNotEmpty() && checkCount <= dayLimit && minuteCount <= minuteLimit && license != null) {
                val address = queue.poll()
                schedule.runAsync {
                    if (ProxyCache.isProxy(address)) return@runAsync
                    val client = ClientHelper(OkHttpClient.Builder(), "http://proxycheck.io/v2$address?key=$license&vpn=${if (checkVPN) 3 else 0}")
                    client.setProxy(useProxy)
                    try {
                        val response = client.getResponse().body!!.string()
                        if (response.contains("\"status\": \"ok\",") || response.contains("\"status\": \"warning\",")) {
                            checked.put(address, true)
                            if (parseProxy(response)) { ProxyCache.addProxy(ProxyResult(address, ProxyResultType.PROXY_CHECK)) }
                        }
                        if (response.contains("\"status\": \"denied\",") && response.contains("\"message\": \"1,000 free queries exhausted. Please try the API again tomorrow or purchase a higher paid plan.\"")) {
                            this.checkCount.set(dayLimit)
                            MessageUtil.logWarn("[MoeFilter] [ProxyCheck] You are reached limit for proxycheck.io!")
                            queuedLimit.addAll(queue)
                            queue.clear()
                        }
                    } catch (ex: Exception) { if (LocalConfig.getConfig().getBoolean("debug")) { ex.printStackTrace() }; queue.add(address) }
                    client.getResponse().close()
                }
            }
        }
        allSchedule.add(checkSchedule!!)
        return true
    }

    private fun parseProxy(input: String): Boolean {
        val result = input.contains("\"proxy\": \"yes\",")
        val point = if (result && checkVPN && input.contains("\"type\": \"VPN\"")) 2 else 1
        this.pointHelper(point, checkCount)
        this.pointHelper(point, minuteCount)
        return result
    }

    private fun pointHelper(point: Int, target: AtomicInteger) { target.set(target.get() + point) }


}