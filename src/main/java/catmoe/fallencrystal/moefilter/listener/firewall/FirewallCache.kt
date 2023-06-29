package catmoe.fallencrystal.moefilter.listener.firewall

import catmoe.fallencrystal.moefilter.util.message.MessageUtil
import com.github.benmanes.caffeine.cache.Caffeine
import java.net.InetAddress
import java.util.concurrent.TimeUnit

object FirewallCache {

    private val cache = Caffeine.newBuilder().build<InetAddress, Boolean>()
    private val tempCache = Caffeine.newBuilder().expireAfterWrite(30, TimeUnit.SECONDS).build<InetAddress, Boolean>()

    fun addAddress(address: InetAddress, status: Boolean?) { cache.put(address, status ?: true); status ?: cache.invalidate(address); logFirewalled(address, false) }

    fun addAddressTemp(address: InetAddress, status: Boolean?) { tempCache.put(address, status ?: true); status?: tempCache.invalidate(address); logFirewalled(address, true) }

    fun removeAddress(address: InetAddress) { cache.invalidate(address) }
    fun isFirewalled(address: InetAddress): Boolean { return cache.getIfPresent(address) ?: false || tempCache.getIfPresent(address) ?: false }

    private fun logFirewalled(address: InetAddress, temp: Boolean) {
        if (isFirewalled(address)) { if (!temp) { MessageUtil.logInfo("[MoeFilter] [AntiBot] $address are firewalled.") } else { MessageUtil.logInfo("[MoeFilter] [AntiBot] $address are temp firewalled (30 seconds)") } }
    }

}