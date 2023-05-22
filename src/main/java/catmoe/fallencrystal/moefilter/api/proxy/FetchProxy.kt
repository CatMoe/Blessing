package catmoe.fallencrystal.moefilter.api.proxy

import catmoe.fallencrystal.moefilter.common.config.ObjectConfig
import catmoe.fallencrystal.moefilter.util.message.MessageUtil
import catmoe.fallencrystal.moefilter.util.plugin.FilterPlugin
import net.md_5.bungee.api.ProxyServer
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.SocketTimeoutException
import java.util.*
import kotlin.concurrent.schedule

class FetchProxy {
    private val proxies = ObjectConfig.getConfig().getStringList("proxy.lists")
    private var count = 0

    fun get() {
        for (it in proxies) {
            ProxyServer.getInstance().scheduler.runAsync(FilterPlugin.getPlugin()) {
                try {
                    val client = OkHttpClient()
                    val request = Request.Builder().url(it).build()
                    val response = client.newCall(request).execute()
                    val regex = Regex("""(\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}):(\d+)""")
                    if (response.isSuccessful) {
                        val lines = response.body?.string()?.split("\n")
                        for (line in lines!!) {
                            val proxy = regex.replace(line.trim()) { matchResult -> val address = matchResult.groupValues[1]
                                address.replace(Regex("[^\\x20-\\x7E]"), "")
                            }
                            ProxyCache.addProxy(proxy)
                            if (ObjectConfig.getConfig().getBoolean("debug")) { MessageUtil.logInfo("[MoeFilter] [ProxyFetch] $proxy has added to list.") }
                            count++
                        }
                    }
                    response.close()
                } catch (ex: SocketTimeoutException) { MessageUtil.logWarn("[MoeFilter] [ProxyFetch] $it has no responded. skipping proxies scammer.") }
            }
        }
        Timer().schedule(30000) { MessageUtil.logInfo("[MoeFilter] [ProxyFetch] get $count proxies.") }
    }
}