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
import catmoe.fallencrystal.moefilter.util.message.v2.MessageUtil
import com.github.benmanes.caffeine.cache.Caffeine
import java.net.InetAddress
import java.util.concurrent.TimeUnit

object FirewallCache {

    private var debug = LocalConfig.getConfig().getBoolean("debug")
    private val cache = Caffeine.newBuilder().build<InetAddress, Boolean>()
    private val tempCache = Caffeine.newBuilder().expireAfterWrite(30, TimeUnit.SECONDS).build<InetAddress, Boolean>()

    fun addAddress(address: InetAddress, status: Boolean?) { cache.put(address, status ?: true); status ?: cache.invalidate(address); logFirewalled(address, false) }

    fun addAddressTemp(address: InetAddress, status: Boolean?) { tempCache.put(address, status ?: true); status?: tempCache.invalidate(address); logFirewalled(address, true) }

    fun removeAddress(address: InetAddress) { cache.invalidate(address) }

    fun isFirewalled(address: InetAddress): Boolean { return cache.getIfPresent(address) ?: false || tempCache.getIfPresent(address) ?: false }

    private fun logFirewalled(address: InetAddress, temp: Boolean) {
        if (!debug) return
        if (isFirewalled(address)) { if (!temp) { MessageUtil.logInfo("[MoeFilter] [AntiBot] $address are firewalled.") } else { MessageUtil.logInfo("[MoeFilter] [AntiBot] $address are temp firewalled (30 seconds)") } }
    }

    fun reload() { debug = LocalConfig.getConfig().getBoolean("debug") }

}