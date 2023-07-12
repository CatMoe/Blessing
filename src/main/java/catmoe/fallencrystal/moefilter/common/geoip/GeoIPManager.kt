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

import catmoe.fallencrystal.moefilter.common.config.LocalConfig
import catmoe.fallencrystal.moefilter.common.geoip.CountryMode.DISABLED
import catmoe.fallencrystal.moefilter.common.geoip.CountryMode.WHITELIST
import com.maxmind.geoip2.DatabaseReader
import java.net.InetAddress
import java.util.concurrent.atomic.AtomicBoolean

@Suppress("unused", "MemberVisibilityCanBePrivate")
object GeoIPManager {
    val available = AtomicBoolean(false)
    private var conf = LocalConfig.getProxy().getConfig("country")
    private var list = conf.getStringList("list")
    private var type = try { CountryMode.valueOf(conf.getAnyRef("mode").toString()) } catch (_: Exception) { DISABLED }
    var country: DatabaseReader? = null
    var city: DatabaseReader? = null

    fun reload() {
        val conf = LocalConfig.getProxy().getConfig("country")
        this.list = conf.getStringList("list")
        this.type = try { CountryMode.valueOf(conf.getAnyRef("mode").toString()) } catch (_: Exception) { DISABLED }
        this.conf = conf
    }

    fun checkCountry(address: InetAddress): Boolean { return checkCountry(address, type) }

    fun checkCountry(address: InetAddress, type: CountryMode): Boolean {
        if (!available.get() || type == DISABLED) return false
        val country = getISOCode(address)
        if (country == "NULL") return false
        return if (type == WHITELIST) !list.contains(country) else list.contains(country)
    }

    fun getISOCode(address: InetAddress): String { return try { (country ?: return "NULL").country(address).country.isoCode } catch (_: Exception) { "NULL" } }
}