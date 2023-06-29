package catmoe.fallencrystal.moefilter.common.check.mixed

import catmoe.fallencrystal.moefilter.common.check.info.CheckInfo
import catmoe.fallencrystal.moefilter.common.check.info.impl.Joining
import catmoe.fallencrystal.moefilter.common.check.info.impl.Pinging
import catmoe.fallencrystal.moefilter.common.check.mixed.MixedType.*
import catmoe.fallencrystal.moefilter.common.config.LocalConfig
import catmoe.fallencrystal.moefilter.listener.firewall.FirewallCache
import catmoe.fallencrystal.moefilter.listener.firewall.Throttler
import catmoe.fallencrystal.moefilter.network.bungee.util.kick.DisconnectType
import catmoe.fallencrystal.moefilter.util.message.MessageUtil
import com.github.benmanes.caffeine.cache.Caffeine
import java.net.InetAddress
import java.util.concurrent.TimeUnit

object MixedCheck {

    private val joinCache = Caffeine.newBuilder().expireAfterWrite(10, TimeUnit.SECONDS).build<InetAddress, String>()
    private val pingCache = Caffeine.newBuilder().expireAfterWrite(10, TimeUnit.SECONDS).build<InetAddress, Boolean>()
    private var type: MixedType = DISABLED

    fun increase(info: CheckInfo): DisconnectType? {
        if (info is Joining) {
            return when (type) {
                RECONNECT -> { if (cacheJoin(info)) null else DisconnectType.REJOIN }
                JOIN_AFTER_PING -> { if (cachePing(info.address, false)) null else DisconnectType.PING }
                RECONNECT_AFTER_PING -> {
                    if (!cachePing(info.address, false)) { DisconnectType.PING }
                    else if (!cacheJoin(info)) { DisconnectType.REJOIN }
                    else null
                }
                PING_AFTER_RECONNECT -> {
                    if (cacheJoin(info)) { if (cachePing(info.address, false)) null else DisconnectType.PING }
                    else { pingCache.invalidate(info.address); DisconnectType.REJOIN }
                }
                STABLE -> {
                    if (!cacheJoin(info)) DisconnectType.REJOIN else if (!cachePing(info.address, false)) DisconnectType.PING else null
                }
                DISABLED -> { null }
            }
        }
        if (info is Pinging) {
            if (Throttler.isThrottled(info.address)) { FirewallCache.addAddressTemp(info.address, true) }
            else { cachePing(info.address, true) }
            return null
        }
        return null
    }

    private fun cacheJoin(info: CheckInfo): Boolean {
        if (info is Joining) { return if (joinCache.getIfPresent(info.address) != null) true else { joinCache.put(info.address, info.username); false } }
        return false
    }

    private fun cachePing(address: InetAddress, write: Boolean): Boolean {
        return if (pingCache.getIfPresent(address) != null) true else { if (write) { pingCache.put(address, write) }; false }
    }

    fun reload() {
        joinCache.invalidateAll()
        pingCache.invalidateAll()
        val configureType = try { LocalConfig.getAntibot().getAnyRef("general.join-ping-mixin-mode").toString() } catch (e: Exception) { MessageUtil.logError("[MoeFilter] [MixedCheck] Failed to get type. That is empty or config file is outdated?"); return }
        this.type = try { MixedType.valueOf(configureType) } catch (e: Exception) { MessageUtil.logWarn("[MoeFilter] [MixedCheck] Unknown mode \"$configureType\", Disabling.."); DISABLED }
    }

}