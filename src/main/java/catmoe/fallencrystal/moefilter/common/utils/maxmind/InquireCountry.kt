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

package catmoe.fallencrystal.moefilter.common.utils.maxmind

import catmoe.fallencrystal.moefilter.MoeFilter
import catmoe.fallencrystal.moefilter.common.config.LocalConfig
import com.maxmind.geoip2.DatabaseReader
import com.maxmind.geoip2.model.CountryResponse
import java.io.File
import java.net.InetAddress

object InquireCountry {
    private val databaseFile = File("${MoeFilter.instance.dataFolder}/geolite/GeoLite2-Country.mmdb")
    private val reader = DatabaseReader.Builder(databaseFile).build()

    private val enabled = try { LocalConfig.getProxy().getBoolean("country.enabled") } catch (_: Exception) { false }

    @Suppress("UNUSED")
    fun check(address: InetAddress): CountryResponse? { return if (enabled) { try { reader.country(address) } catch (_: Exception) { null } } else { null } }
}