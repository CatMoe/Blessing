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