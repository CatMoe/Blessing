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

package catmoe.fallencrystal.moefilter.common.geoip

import catmoe.fallencrystal.moefilter.common.geoip.CountryMode.DISABLED
import catmoe.fallencrystal.moefilter.common.geoip.CountryMode.WHITELIST
import catmoe.fallencrystal.translation.utils.config.IgnoreInitReload
import catmoe.fallencrystal.translation.utils.config.LocalConfig
import catmoe.fallencrystal.translation.utils.config.Reloadable
import com.github.benmanes.caffeine.cache.Caffeine
import com.maxmind.geoip2.DatabaseReader
import java.net.InetAddress
import java.util.concurrent.atomic.AtomicBoolean

@IgnoreInitReload
@Suppress("MemberVisibilityCanBePrivate")
object GeoIPManager : Reloadable {
    val available = AtomicBoolean(false)
    private var conf = LocalConfig.getProxy().getConfig("country")
    private var list = conf.getStringList("list")
    private var type = try { CountryMode.valueOf(conf.getAnyRef("mode").toString()) } catch (_: Exception) { DISABLED }
    var country: DatabaseReader? = null
    var city: DatabaseReader? = null
    private val cache = Caffeine.newBuilder().build<String, Boolean>()
    private val regex = "^(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})\\.\\d{1,3}\$".toRegex()

    override fun reload() {
        val conf = LocalConfig.getProxy().getConfig("country")
        this.list = conf.getStringList("list")
        val type = try { CountryMode.valueOf(conf.getAnyRef("mode").toString()) } catch (_: Exception) { DISABLED }
        this.conf = conf
        if (this.type != type) {
            cache.invalidateAll()
            this.type=type
        }
    }

    fun checkCountry(address: InetAddress): Boolean { return checkCountry(address, type) }

    fun checkCountry(address: InetAddress, type: CountryMode): Boolean {
        if (!available.get() || type == DISABLED) return false
        val m = regex.find(address.hostAddress)
        if (m != null && cache.getIfPresent(m.value) == true) return true
        val country = getISOCode(address)
        if (country == "NULL") return false
        val result = if (type == WHITELIST) !list.contains(country) else list.contains(country)
        if (result && m != null) cache.put(m.value, true)
        return result
    }

    fun getISOCode(address: InetAddress): String { return try { (country ?: return "NULL").country(address).country.isoCode } catch (_: Exception) { "NULL" } }
}