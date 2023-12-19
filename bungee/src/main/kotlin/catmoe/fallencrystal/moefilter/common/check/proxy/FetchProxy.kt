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

package catmoe.fallencrystal.moefilter.common.check.proxy

import catmoe.fallencrystal.moefilter.MoeFilterBungee
import catmoe.fallencrystal.moefilter.common.check.proxy.type.ProxyResult
import catmoe.fallencrystal.moefilter.common.check.proxy.type.ProxyResultType
import catmoe.fallencrystal.moefilter.util.message.v2.MessageUtil
import catmoe.fallencrystal.moefilter.util.plugin.ClientHelper
import catmoe.fallencrystal.moefilter.util.plugin.util.Scheduler
import catmoe.fallencrystal.translation.utils.config.LocalConfig
import okhttp3.OkHttpClient
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.schedule

class FetchProxy {
    private val plugin = MoeFilterBungee.instance

    private var config = LocalConfig.getProxy()
    private var proxies = config.getStringList("internal.lists")
    private var debug = config.getBoolean("internal.debug")
    private val updateDelay = config.getLong("internal.schedule.update-delay")
    private var count = 0
    private val scheduler = Scheduler(plugin)
    private var isAlreadyStopped = AtomicBoolean(false)

    private val scheduleTaskId = AtomicInteger(0)

    private fun initSchedule() {
        if (scheduleTaskId.get() != 0) { scheduler.cancelTask(scheduleTaskId.get()); scheduleTaskId.set(0) }
        val schedule = Scheduler(plugin).repeatScheduler(updateDelay, TimeUnit.HOURS) { get() }
        scheduleTaskId.set(schedule.id)
    }

    fun get() { get(proxies) }

    fun get(lists: List<String>) {
        MessageUtil.logInfo("[MoeFilter] [ProxyFetch] Starting Async proxy fetcher. (${proxies.size} Threads)")
        for (it in lists) {
            scheduler.runAsync {
                try {
                    val client = ClientHelper(OkHttpClient.Builder(), it)
                    client.setProxy(true)
                    val response = client.getResponse()
                    if (response.isSuccessful) {
                        val lines = response.body?.string()?.split("\n")
                        for (line in lines!!) {
                            val proxy = regex.replace(line.trim()) { matchResult -> val address = matchResult.groupValues[1]
                                address.replace(Regex("[^\\x20-\\x7E]"), "") }
                            try { if (!ProxyCache.isProxy(InetAddress.getByName(proxy))) {
                                ProxyCache.addProxy(ProxyResult(InetAddress.getByName(proxy), ProxyResultType.INTERNAL))
                                if (debug) { MessageUtil.logInfo("[MoeFilter] [ProxyFetch] $proxy has added to list. (from $it)") }
                                count++
                            } } catch (ex: UnknownHostException) { if (debug) MessageUtil.logWarn("[MoeFilter] [ProxyFetch] $proxy is not a valid address. (from $it)") }
                        }
                    }
                    response.close()
                }
                catch (ex: Exception) { MessageUtil.logWarn("[MoeFilter] [ProxyFetch] failed get proxies list from $it : ${ex.localizedMessage}") }
            }
        }
        Timer().schedule(30000) { MessageUtil.logInfo("[MoeFilter] [ProxyFetch] get $count proxies."); count=0 }
    }

    fun reload() {
        if (isAlreadyStopped.get()) return
        val config = LocalConfig.getProxy()
        val proxies = config.getStringList("internal.lists")
        val enabled = config.getBoolean("internal.enabled")
        if (scheduleTaskId.get() != 0) {
            if (!enabled) {
                MessageUtil.logWarn("[MoeFilter] [ProxyFetch] ProxyFetch are disabled. All firewalled proxies from ProxyFetch will clear when restarted server.")
                MessageUtil.logInfo("[MoeFilter] [ProxyFetch] Schedule are stopped.")
                scheduler.cancelTask(scheduleTaskId.get()); scheduleTaskId.set(0); return
            }
            if (this.proxies != proxies || this.config.getInt("internal.schedule.update-delay") != config.getInt("internal.schedule.update-delay")) {
                MessageUtil.logInfo("[MoeFilter] [ProxyFetch] Scheduler update delay are edited or proxies source are edited. Force run update task now..")
                initSchedule()
            }
        } else { if (enabled) { initSchedule() } }
        this.debug = config.getBoolean("internal.debug")
        this.config = config
    }

    companion object {
        val regex = Regex("""(\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}):(\d+)""")
    }
}