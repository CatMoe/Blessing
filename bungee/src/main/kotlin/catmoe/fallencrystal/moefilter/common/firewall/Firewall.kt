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