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

package catmoe.fallencrystal.moefilter.common.firewall

import catmoe.fallencrystal.translation.logger.CubeLogger
import catmoe.fallencrystal.translation.utils.config.LocalConfig
import catmoe.fallencrystal.translation.utils.config.Reloadable
import com.github.benmanes.caffeine.cache.Caffeine
import java.net.InetAddress
import java.util.concurrent.TimeUnit
import java.util.logging.Level

object Throttler : Reloadable {
    val ipCache = Caffeine.newBuilder().expireAfterWrite(1, TimeUnit.SECONDS).build<InetAddress, Int>()
    private var debug = LocalConfig.getConfig().getBoolean("debug")

    private var throttle = 3

    fun increase(address: InetAddress): Boolean {
        val count = ipCache.getIfPresent(address) ?: 0
        ipCache.put(address, count + 1)
        val throttled = count >= throttle
        if (debug && throttled) {
            CubeLogger.log(Level.WARNING, "[Throttler] $address is throttled.")
            try { throw Throwable() } catch (debug: Throwable) { debug.printStackTrace() }
        }
        return throttled
    }

    fun isThrottled(address: InetAddress): Boolean { return (ipCache.getIfPresent(address) ?: 0) >= throttle }

    override fun reload() {
        throttle = LocalConfig.getConfig().getInt("throttle-limit")
        ipCache.invalidateAll()
        debug = LocalConfig.getConfig().getBoolean("debug")
    }
}