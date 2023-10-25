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

package catmoe.fallencrystal.moefilter.common.check.proxy.util

import catmoe.fallencrystal.translation.utils.config.LocalConfig
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