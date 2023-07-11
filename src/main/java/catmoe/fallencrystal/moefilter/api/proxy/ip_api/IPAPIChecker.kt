/*
 * Copyright 2023. CatMoe / FallenCrystal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package catmoe.fallencrystal.moefilter.api.proxy.ip_api

import catmoe.fallencrystal.moefilter.MoeFilter
import catmoe.fallencrystal.moefilter.api.proxy.ProxyCache
import catmoe.fallencrystal.moefilter.common.config.LocalConfig
import catmoe.fallencrystal.moefilter.common.utils.proxy.type.ProxyResult
import catmoe.fallencrystal.moefilter.common.utils.proxy.type.ProxyResultType
import catmoe.fallencrystal.moefilter.util.plugin.util.Scheduler
import com.github.benmanes.caffeine.cache.Caffeine
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.*
import java.util.concurrent.TimeUnit

object IPAPIChecker {

    private val checked = Caffeine.newBuilder().build<InetAddress, Boolean>()
    private val queue: Queue<InetAddress> = ArrayDeque()

    fun schedule() {
        val schedule = Scheduler(MoeFilter.instance)
        // 1500 delay. Prevent to reached call limit.
        schedule.repeatScheduler(1500, 1500 , TimeUnit.MILLISECONDS) {
            if (queue.isNotEmpty()) {
                val address = queue.poll()
                schedule.runAsync { if (check(address)) { ProxyCache.addProxy(ProxyResult(address, ProxyResultType.IP_API)) }; checked.put(address, true) }
            }
        }
    }

    fun addAddress(address: InetAddress) { if (checked.getIfPresent(address) != null || queue.contains(address) || ProxyCache.isProxy(address)) return else queue.add(address) }

    fun check(address: InetAddress): Boolean {
        if (checked.getIfPresent(address) == true) { return ProxyCache.isProxy(address) }
        // field 147456 = status, proxy
        // I'm actually too lazy to actually parse those xml/json. I just need to use .contains() to try to match them.
        // InetAddress .toString -> /xx.xx.xx.xx. So that actually like http://ip-api.com/xml/127.0.0.1?fields=147456
        val url = "http://ip-api.com/xml$address?fields=147456"
        val client = OkHttpClient().newBuilder()
        val conf = LocalConfig.getProxy().getConfig("proxies-config")
        val proxyType = try { (Proxy.Type.valueOf(conf.getAnyRef("mode").toString())) } catch (_: IllegalArgumentException) { Proxy.Type.DIRECT }
        if (proxyType != Proxy.Type.DIRECT) { client.proxy(Proxy(proxyType, InetSocketAddress(conf.getString("host"), conf.getInt("port")))) }
        try {
            val response = client.build().newCall(Request.Builder().url(url).build()).execute()
            if (response.isSuccessful) {
                val text = response.body!!.string()
                if (text.contains("<status>success</status>")) { return text.contains("<proxy>true</proxy>") }
            }
            response.close()
        } catch (_: Exception) { return false }
        return false
    }

}