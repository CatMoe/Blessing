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