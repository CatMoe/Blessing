package catmoe.fallencrystal.moefilter.api.proxy

import catmoe.fallencrystal.moefilter.common.config.ObjectConfig
import catmoe.fallencrystal.moefilter.common.utils.proxy.type.ProxyResult
import catmoe.fallencrystal.moefilter.common.utils.proxy.type.ProxyResultType
import catmoe.fallencrystal.moefilter.util.message.MessageUtil
import catmoe.fallencrystal.moefilter.util.plugin.FilterPlugin
import net.md_5.bungee.api.ProxyServer
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.*
import java.util.*
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLHandshakeException
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
        if (config.getBoolean("proxies-config.enabled")) { MessageUtil.logInfo("[MoeFilter] [ProxyFetch] Applying HTTP proxy to help fetch proxies.") }
        MessageUtil.logInfo("[MoeFilter] [ProxyFetch] Starting Async proxy fetcher. (${proxies.size} Threads)")
        for (it in lists) {
            ProxyServer.getInstance().scheduler.runAsync(FilterPlugin.getPlugin()) {
                try {
                    val client = OkHttpClient().newBuilder()
                    if (config.getBoolean("proxies-config.enabled")) {
                        val proxyConfig = Proxy(Proxy.Type.HTTP, InetSocketAddress(config.getString("proxies-config.host"), config.getInt("proxies-config.port")))
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
                catch (ex: SocketTimeoutException) { MessageUtil.logWarnRaw("[MoeFilter] [ProxyFetch] $it has no responded. skipping proxies scammer.") }
                catch (ex: ConnectException) { MessageUtil.logWarnRaw("[MoeFilter] [ProxyFetch] Failed to connect $it. Your server is offline or target is unavailable?") }
                catch (ex: SSLHandshakeException) { MessageUtil.logWarnRaw("[MoeFilter] [ProxyFetch] Failed to connect $it. Target server SSL handshake is unavailable.") }
                catch (ex: UnknownHostException) { MessageUtil.logWarnRaw("[MoeFilter] [ProxyFetch] $it is unknown host. Please check your internet.") }
                catch (ex: SocketException) { MessageUtil.logWarnRaw("[MoeFilter] [ProxyFetch] $it rejected connection.") }
            }
        }
        Timer().schedule(30000) { MessageUtil.logInfo("[MoeFilter] [ProxyFetch] get $count proxies.") }
    }
}