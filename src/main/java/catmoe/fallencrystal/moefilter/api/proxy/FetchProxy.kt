package catmoe.fallencrystal.moefilter.api.proxy

import catmoe.fallencrystal.moefilter.MoeFilter
import catmoe.fallencrystal.moefilter.common.config.LocalConfig
import catmoe.fallencrystal.moefilter.common.utils.proxy.type.ProxyResult
import catmoe.fallencrystal.moefilter.common.utils.proxy.type.ProxyResultType
import catmoe.fallencrystal.moefilter.util.message.MessageUtil
import catmoe.fallencrystal.moefilter.util.plugin.util.Scheduler
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.UnknownHostException
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.schedule

class FetchProxy {
    private val plugin = MoeFilter.instance

    private var config = LocalConfig.getProxy()
    private var proxies = config.getStringList("internal.lists")
    private var debug = config.getBoolean("internal.debug")
    private val updateDelay = config.getInt("internal.schedule.update-delay").toLong()
    private var count = 0
    private val scheduler = Scheduler(plugin)

    private var proxyType = updateProxyType()
    private val scheduleTaskId = AtomicInteger(0)

    private fun initSchedule() {
        if (scheduleTaskId.get() != 0) {
            scheduler.cancelTask(scheduleTaskId.get())
            scheduleTaskId.set(0)
        }
        val schedule = Scheduler(plugin).repeatScheduler(updateDelay, TimeUnit.HOURS) { get() }
        scheduleTaskId.set(schedule.id)
    }

    private fun updateProxyType(): Proxy.Type {
        val proxyType=(try { Proxy.Type.valueOf(config.getAnyRef("proxies-config.mode").toString()) } catch (ex: Exception) { MessageUtil.logWarn("[MoeFilter] [FetchProxy] Unknown proxy type ${config.getAnyRef("proxies-config.mode")}, Fallback to DIRECT."); Proxy.Type.DIRECT } )
        this.proxyType=proxyType
        return proxyType
    }

    fun get() { get(proxies) }

    fun get(lists: List<String>) {
        MessageUtil.logInfo("[MoeFilter] [ProxyFetch] Starting Async proxy fetcher. (${proxies.size} Threads)")
        if (proxyType != Proxy.Type.DIRECT) { MessageUtil.logInfo("[MoeFilter] [ProxyFetch] Applying HTTP proxy to help fetch proxies.") }
        for (it in lists) {
            scheduler.runAsync {
                try {
                    val client = OkHttpClient().newBuilder()
                    if (proxyType != Proxy.Type.DIRECT) {
                        val proxyConfig = Proxy(proxyType, InetSocketAddress(config.getString("proxies-config.host"), config.getInt("proxies-config.port")))
                        client.proxy(proxyConfig)
                    }
                    val call = client.build()
                    val request = Request.Builder().url(it).build()
                    val response = call.newCall(request).execute()
                    val regex = Regex("""(\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}):(\d+)""")
                    if (response.isSuccessful) {
                        val lines = response.body?.string()?.split("\n")
                        for (line in lines!!) {
                            val proxy = regex.replace(line.trim()) { matchResult -> val address = matchResult.groupValues[1]
                                address.replace(Regex("[^\\x20-\\x7E]"), "") }
                            try { if (!ProxyCache.isProxy(InetAddress.getByName(proxy))) {
                                ProxyCache.addProxy(ProxyResult(InetAddress.getByName(proxy), ProxyResultType.INTERNAL))
                                if (debug) { MessageUtil.logInfo("[MoeFilter] [ProxyFetch] $proxy has added to list. (from $it)") }
                                count++
                            } } catch (ex: UnknownHostException) { MessageUtil.logWarnRaw("[MoeFilter] [ProxyFetch] $proxy is not a valid address. (from $it)"); }
                        }
                    }
                    response.close()
                }
                catch (ex: Exception) { MessageUtil.logWarnRaw("[MoeFilter] [ProxyFetch] failed get proxies list from $it : ${ex.localizedMessage}") }
            }
        }
        Timer().schedule(30000) { MessageUtil.logInfo("[MoeFilter] [ProxyFetch] get $count proxies."); count=0 }
    }

    fun reload() {
        val config = LocalConfig.getProxy()
        val proxies = config.getStringList("internal.lists")
        val enabled = config.getBoolean("internal.enabled")
        if (scheduleTaskId.get() != 0) {
            if (!enabled) {
                MessageUtil.logWarn("[MoeFilter] [ProxyFetch] ProxyFetch are disabled. All firewalled proxies from ProxyFetch will clear when restarted server.")
                MessageUtil.logInfo("[MoeFilter] [ProxyFetch] Schedule are stopped.")
                scheduler.cancelTask(scheduleTaskId.get()); return
            }
            if (this.proxies != proxies || this.config.getInt("internal.schedule.update-delay") != config.getInt("internal.schedule.update-delay")) {
                MessageUtil.logInfo("[MoeFilter] [ProxyFetch] Scheduler update delay are edited or proxies source are edited. Force run update task now..")
                initSchedule()
            }
        } else { if (enabled) { initSchedule() } }
        this.debug = config.getBoolean("internal.debug")
        this.config = config
        updateProxyType()
    }
}