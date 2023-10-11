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

package catmoe.fallencrystal.moefilter.common.firewall

import catmoe.fallencrystal.moefilter.common.firewall.FirewallType.*
import catmoe.fallencrystal.moefilter.common.firewall.lockdown.LockdownManager
import catmoe.fallencrystal.moefilter.common.firewall.system.ExecutorHelper
import catmoe.fallencrystal.moefilter.common.firewall.system.FirewallLoader
import catmoe.fallencrystal.moefilter.util.message.v2.MessageUtil
import catmoe.fallencrystal.translation.utils.config.LocalConfig
import catmoe.fallencrystal.translation.utils.config.Reloadable
import com.github.benmanes.caffeine.cache.Caffeine
import java.net.InetAddress
import java.util.concurrent.TimeUnit

object Firewall : Reloadable {

    private var config = LocalConfig.getAntibot().getConfig("firewall")
    private var debug = LocalConfig.getConfig().getBoolean("debug")
    val cache = Caffeine.newBuilder().build<InetAddress, Boolean>()
    val tempCache = Caffeine.newBuilder().expireAfterWrite(config.getLong("temp-expire-time"), TimeUnit.SECONDS).build<InetAddress, Boolean>()
    private val osFirewall = FirewallLoader("MoeFilter", 300)
    @Suppress("SpellCheckingInspection")
    private val executor = ExecutorHelper("sudo ipset add MoeFilter {chain}")
    private var mode = FirewallType.valueOf(config.getAnyRef("mode").toString())

    fun addAddress(address: InetAddress) {
        when (mode) {
            INTERNAL ->  { addAddressL7(address) }
            SYSTEM -> { executor.addToQueue(addressToString(address)) }
            INTERNAL_AND_SYSTEM -> { addAddressL7(address); executor.addToQueue(addressToString(address)) }
        }
        logFirewalled(address , false)
    }

    private fun addAddressL7(address: InetAddress) { cache.put(address, true); logFirewalled(address, false) }

    fun addAddressTemp(address: InetAddress) {
        when (mode) {
            INTERNAL -> { tempCache.put(address, true) }
            SYSTEM -> { executor.addToQueue(addressToString(address)) }
            INTERNAL_AND_SYSTEM -> { tempCache.put(address, true); executor.addToQueue(addressToString(address)) }
        }
        logFirewalled(address, true)
    }

    @Suppress("unused")
    fun removeAddress(address: InetAddress) { cache.invalidate(address) }

    fun isFirewalled(address: InetAddress): Boolean {
        return if (LockdownManager.state.get()) !LockdownManager.verify(address) else cache.getIfPresent(address) ?: false || tempCache.getIfPresent(address) ?: false
    }

    private fun logFirewalled(address: InetAddress, temp: Boolean) {
        if (!debug) return
        if (isFirewalled(address)) { if (!temp) { MessageUtil.logInfo("[MoeFilter] [AntiBot] $address are firewalled.") } else { MessageUtil.logInfo("[MoeFilter] [AntiBot] $address are temp firewalled (30 seconds)") } }
        Throwable().printStackTrace()
    }

    private fun addressToString(address: InetAddress): String { return address.toString().replace("/", "") }

    fun load() {
        reload()
        if (mode != INTERNAL) { loadSystem() }
    }

    private fun loadSystem() {
        osFirewall.initFirewall()
        executor.maxSize.set(100000)
        executor.debug.set(true)
    }

    override fun reload() {
        this.config = LocalConfig.getAntibot().getConfig("firewall")
        debug = LocalConfig.getConfig().getBoolean("debug")
        mode = FirewallType.valueOf(config.getAnyRef("mode").toString())
        executor.debug.set(debug)
    }

    fun shutdown() {
        if (mode != INTERNAL) { osFirewall.stop(); executor.shutdown() }
    }

}