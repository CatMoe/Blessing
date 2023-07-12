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

package catmoe.fallencrystal.moefilter.common.check.proxy.util

import catmoe.fallencrystal.moefilter.common.config.LocalConfig
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.net.InetSocketAddress
import java.net.Proxy

class ClientHelper(private val c: OkHttpClient.Builder, private val url: String) {

    private val proxyConf = LocalConfig.getProxy().getConfig("proxies-config")
    private val proxyType = try { (Proxy.Type.valueOf(proxyConf.getAnyRef("mode").toString())) } catch (_: IllegalArgumentException) { Proxy.Type.DIRECT }

    fun setProxy(boolean: Boolean) {
        if (boolean) {
            if (proxyType != Proxy.Type.DIRECT) { c.proxy(Proxy(proxyType, InetSocketAddress(proxyConf.getString("host"), proxyConf.getInt("port")))) }
        } else {
            c.proxy(Proxy.NO_PROXY)
        }
    }

    fun getResponse(): Response { return c.build().newCall(Request.Builder().url(url).build()).execute() }
}