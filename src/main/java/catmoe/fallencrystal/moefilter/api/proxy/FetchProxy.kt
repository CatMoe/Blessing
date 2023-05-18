package catmoe.fallencrystal.moefilter.api.proxy

import catmoe.fallencrystal.moefilter.common.config.ObjectConfig
import catmoe.fallencrystal.moefilter.util.plugin.FilterPlugin
import net.md_5.bungee.api.ProxyServer
import okhttp3.OkHttpClient
import okhttp3.Request

class FetchProxy {
    val proxies = ObjectConfig.getConfig().getStringList("proxy.lists")

    fun get() {
        for (it in proxies) {
            ProxyServer.getInstance().scheduler.runAsync(FilterPlugin.getPlugin()) {
                val client = OkHttpClient()
                val request = Request.Builder().url(it).build()
                val response = client.newCall(request).execute()
                val regex = Regex("""(\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}):(\d+)""")
                if (response.isSuccessful) {
                    val lines = response.body?.string()?.split("\n")
                    for (line in lines!!) {
                        val proxy = regex.replace(line) { matchResult -> val address = matchResult.groupValues[1]
                        address
                        }
                        ProxyCache.addProxy(proxy)
                    }
                }
            }
        }
    }
}