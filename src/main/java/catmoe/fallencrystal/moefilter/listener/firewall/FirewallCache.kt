package catmoe.fallencrystal.moefilter.listener.firewall

import catmoe.fallencrystal.moefilter.util.message.MessageUtil
import com.github.benmanes.caffeine.cache.Caffeine
import java.net.InetAddress

object FirewallCache {

    private val cache = Caffeine.newBuilder().build<InetAddress, Boolean>()

    fun addAddress(address: InetAddress, status: Boolean?) { cache.put(address, status ?: true); status ?: cache.invalidate(address); MessageUtil.logInfo("[MoeFilter] [AntiBot] $address are firewalled.") }

    fun removeAddress(address: InetAddress) { cache.invalidate(address) }
    fun isFirewalled(address: InetAddress): Boolean { return cache.getIfPresent(address) ?: false }

}