package catmoe.fallencrystal.moefilter.api.proxy

import catmoe.fallencrystal.moefilter.common.config.ObjectConfig
import catmoe.fallencrystal.moefilter.common.utils.proxy.type.ProxyResult
import catmoe.fallencrystal.moefilter.common.utils.proxy.type.ProxyResultType
import catmoe.fallencrystal.moefilter.util.message.MessageUtil
import catmoe.fallencrystal.moefilter.util.plugin.FilterPlugin
import net.md_5.bungee.api.ProxyServer
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.ConnectException
import java.net.InetAddress
import java.net.SocketTimeoutException
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

    init { if (config.getBoolean("internal.enabled")) { ProxyServer.getInstance().scheduler.schedule(FilterPlugin.getPlugin(), { get() }, updateDelay, TimeUnit.HOURS ) } }

    fun get() { get(proxies) }

    fun get(lists: List<String>) {
        for (it in lists) {
            ProxyServer.getInstance().scheduler.runAsync(FilterPlugin.getPlugin()) {
                try {
                    val client = OkHttpClient()
                    val request = Request.Builder().url(it).build()
                    val response = client.newCall(request).execute()
                    val regex = Regex("""(\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}):(\d+)""")
                    if (response.isSuccessful) {
                        val lines = response.body?.string()?.split("\n")
                        for (line in lines!!) {
                            val proxy = regex.replace(line.trim()) { matchResult -> val address = matchResult.groupValues[1]; address.replace(Regex("[^\\x20-\\x7E]"), "") }
                            try { if (ProxyCache.isProxy(InetAddress.getByName(proxy))) return@runAsync } catch (ex: UnknownHostException) { MessageUtil.logWarn("$proxy is not a valid address."); return@runAsync }
                            ProxyCache.addProxy(ProxyResult(InetAddress.getByName(proxy), ProxyResultType.INTERNAL))
                            if (debug) { MessageUtil.logInfo("[MoeFilter] [ProxyFetch] $proxy has added to list.") }
                            count++
                        }
                    }
                    response.close()
                }
                catch (ex: SocketTimeoutException) { MessageUtil.logWarn("[MoeFilter] [ProxyFetch] $it has no responded. skipping proxies scammer.") }
                catch (ex: ConnectException) { MessageUtil.logWarn("[MoeFilter] [ProxyFetch] Failed to connection $it. Your server is offline or target is downed?") }
            }
        }
        Timer().schedule(30000) { MessageUtil.logInfo("[MoeFilter] [ProxyFetch] get $count proxies.") }
    }
}