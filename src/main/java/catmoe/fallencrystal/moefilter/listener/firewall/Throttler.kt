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

package catmoe.fallencrystal.moefilter.listener.firewall

import catmoe.fallencrystal.moefilter.common.config.LocalConfig
import com.github.benmanes.caffeine.cache.Caffeine
import java.net.InetAddress
import java.util.concurrent.TimeUnit

object Throttler {
    private val ipCache = Caffeine.newBuilder().expireAfterWrite(1, TimeUnit.SECONDS).build<InetAddress, Int>()

    private var throttle = 3

    fun increase(address: InetAddress): Boolean {
        val count = ipCache.getIfPresent(address) ?: 0
        ipCache.put(address, count + 1)
        return count >= throttle
    }

    fun isThrottled(address: InetAddress): Boolean { return (ipCache.getIfPresent(address) ?: 0) >= throttle }

    fun reload() { this.throttle=LocalConfig.getConfig().getInt("throttle-limit") }
}