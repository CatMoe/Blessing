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

package catmoe.fallencrystal.moefilter.network.common.traffic

import com.typesafe.config.Config

class LimiterProfile(
    private val sizeLimit: Int,
    private val interval: Double,
    private val maxPacketRate: Double,
) {

    fun createLimiter(silentException: Boolean): TrafficLimiter { return TrafficLimiter(sizeLimit, interval, maxPacketRate, silentException) }

    companion object {
        fun readConfig(config: Config): LimiterProfile? {
            return if (!config.getBoolean("apply")) null else LimiterProfile(
                config.getInt("size-limit"),
                config.getDouble("interval"),
                config.getDouble("max-packet-rate")
            )
        }
    }

}