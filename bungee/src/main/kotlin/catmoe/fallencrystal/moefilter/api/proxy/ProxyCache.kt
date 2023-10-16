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

package catmoe.fallencrystal.moefilter.api.proxy

import catmoe.fallencrystal.moefilter.common.check.proxy.type.ProxyResult
import catmoe.fallencrystal.translation.utils.config.Reloadable
import com.github.benmanes.caffeine.cache.Caffeine
import java.net.InetAddress

object ProxyCache : Reloadable {
    val cache = Caffeine.newBuilder().build<InetAddress, ProxyResult>()
    private val whitelistedAddress = listOf("127.0.0.1")

    private val fetchProxy = FetchProxy()

    override fun reload() { fetchProxy.reload() }

    fun isProxy(address: InetAddress): Boolean {
        if (whitelistedAddress.contains(address.hostAddress)) return false
        return cache.getIfPresent(address) != null
    }

    fun getProxy(address: InetAddress): ProxyResult? {
        if (whitelistedAddress.contains(address.hostAddress)) return null
        return cache.getIfPresent(address)
    }

    fun addProxy(proxy: ProxyResult) { cache.put(proxy.ip, proxy) }
}