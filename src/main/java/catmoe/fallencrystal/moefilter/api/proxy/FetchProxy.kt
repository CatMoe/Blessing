package catmoe.fallencrystal.moefilter.api.proxy

import catmoe.fallencrystal.moefilter.common.config.ObjectConfig
import catmoe.fallencrystal.moefilter.common.utils.proxy.type.ProxyResult
import catmoe.fallencrystal.moefilter.common.utils.proxy.type.ProxyResultType
import catmoe.fallencrystal.moefilter.util.message.MessageUtil
import catmoe.fallencrystal.moefilter.util.plugin.FilterPlugin
import catmoe.fallencrystal.moefilter.util.plugin.util.Scheduler
import net.md_5.bungee.api.ProxyServer
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.UnknownHostException
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.schedule

class FetchProxy {
    private val config = ObjectConfig.getProxy()
    private val proxies = config.getStringList("internal.lists")
    private val debug = config.getBoolean("internal.debug")
    private val updateDelay = config.getInt("internal.schedule.update-delay").toLong()
    private var count = 0

    init { if (config.getBoolean("internal.enabled")) { Scheduler(FilterPlugin.getPlugin()!!).repeatScheduler(updateDelay, TimeUnit.HOURS) { get() } } }

    fun get() { get(proxies) }

    fun get(lists: List<String>) {
        MessageUtil.logInfo("[MoeFilter] [ProxyFetch] Starting Async proxy fetcher. (${proxies.size} Threads)")
        for (it in lists) {
            ProxyServer.getInstance().scheduler.runAsync(FilterPlugin.getPlugin()) {
                try {
                    val client = OkHttpClient().newBuilder()
                    val proxyType: Proxy.Type = (try { Proxy.Type.valueOf(config.getAnyRef("proxies-config.mode").toString()) } catch (ex: Exception) { MessageUtil.logWarn("[MoeFilter] [FetchProxy] Unknown proxy type ${config.getAnyRef("proxies-config.mode")}, Fallback to DIRECT."); Proxy.Type.DIRECT } )
                    if (proxyType != Proxy.Type.DIRECT) { MessageUtil.logInfo("[MoeFilter] [ProxyFetch] Applying HTTP proxy to help fetch proxies.") }
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
                                if (debug) { MessageUtil.logInfo("[MoeFilter] [ProxyFetch] $proxy has added to list.") }
                                count++
                            } } catch (ex: UnknownHostException) { MessageUtil.logWarnRaw("[MoeFilter] [ProxyFetch] $proxy is not a valid address."); }
                        }
                    }
                    response.close()
                }
                catch (ex: Exception) { MessageUtil.logWarnRaw("[MoeFilter] [ProxyFetch] failed get proxies list from $it : ${ex.localizedMessage}") }
            }
        }
        Timer().schedule(30000) { MessageUtil.logInfo("[MoeFilter] [ProxyFetch] get $count proxies.") }
    }
}