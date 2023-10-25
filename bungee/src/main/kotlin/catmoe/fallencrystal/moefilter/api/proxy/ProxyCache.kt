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