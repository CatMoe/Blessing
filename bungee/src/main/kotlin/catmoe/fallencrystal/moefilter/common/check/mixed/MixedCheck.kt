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

package catmoe.fallencrystal.moefilter.common.check.mixed

import catmoe.fallencrystal.moefilter.check.info.CheckInfo
import catmoe.fallencrystal.moefilter.check.info.impl.Joining
import catmoe.fallencrystal.moefilter.check.info.impl.Pinging
import catmoe.fallencrystal.moefilter.common.check.mixed.MixedType.*
import catmoe.fallencrystal.moefilter.common.firewall.Firewall
import catmoe.fallencrystal.moefilter.common.firewall.Throttler
import catmoe.fallencrystal.moefilter.common.state.StateManager
import catmoe.fallencrystal.moefilter.network.common.kick.DisconnectType
import catmoe.fallencrystal.moefilter.network.limbo.util.BungeeSwitcher
import catmoe.fallencrystal.moefilter.util.message.v2.MessageUtil
import catmoe.fallencrystal.translation.utils.config.IgnoreInitReload
import catmoe.fallencrystal.translation.utils.config.LocalConfig
import catmoe.fallencrystal.translation.utils.config.Reloadable
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.RemovalCause
import com.typesafe.config.ConfigException
import java.net.InetAddress
import java.util.concurrent.TimeUnit

@IgnoreInitReload
object MixedCheck : Reloadable {

    private var conf = LocalConfig.getAntibot().getConfig("mixed-check")
    private var maxCacheTime = conf.getLong("max-cache-time")
    private var cache = Caffeine.newBuilder().expireAfterWrite(maxCacheTime, TimeUnit.SECONDS)

    private var suspicionDecay = conf.getLong("blacklist.decay")
    private var blacklistSuspicion = conf.getInt("blacklist.suspicion")
    private var suspicionOnlyInAttack = conf.getBoolean("blacklist.only-in-attack")

    private var protocolCache = cache.build<InetAddress, Int>()

    private var joinCache = cache.build<InetAddress, Joining>()
    private var pingCache = cache.build<InetAddress, Boolean>()

    private var suspicionCount = Caffeine.newBuilder()
        .expireAfterWrite(suspicionDecay, TimeUnit.SECONDS)
        // 性能损耗 D: 虽然不确定是否该使用额外的时间戳的方式来处理suspicion 但或许比用一堆异步+TimerTask要好得多...
        .removalListener { address: InetAddress?, suspicion: Int?, cause: RemovalCause? -> suspicionEvictionListener(address, suspicion, cause) }
        .build<InetAddress, Int>()

    private fun suspicionEvictionListener(address: InetAddress?, suspicion: Int?, cause: RemovalCause?) {
        if (suspicion == null || suspicion == 1 || address == null || cause != RemovalCause.EXPIRED) return
        if (suspicion >= blacklistSuspicion) {
            if (suspicionOnlyInAttack) { if (StateManager.inAttack.get()) Firewall.addAddressTemp(address) }
            else { Firewall.addAddressTemp(address) }; suspicionCount.invalidate(address); return }
        suspicionAdd(address, suspicion - 1)
    }

    // 我认为不需要考虑性能问题 对于 线程数量>=2的vps来讲 原子操作通常快得多 不需要此类内置布尔值
    private val type: MixedType get() = if (StateManager.inAttack.get()) attackType else defaultType

    private var defaultType = loadType("default")
    private var attackType = loadType("in-attack")

    private fun suspicionAdd(address: InetAddress, suspicion: Int?) {
        val s = suspicion ?: (suspicionCount.getIfPresent(address)?.plus(1)) ?: 1
        suspicionCount.put(address, s)
    }

    fun increase(info: CheckInfo): DisconnectType? {
        if (info is Joining) {
            val address = info.address
            // if (MoeLimbo.bungeeQueue.getIfPresent(address) != null) return null
            if (BungeeSwitcher.connectToBungee(address)) {
                return if (BungeeSwitcher.verify(info)) null else {
                    joinCache.put(address, info) // Rewrite info but add suspicion point.
                    pingCache.invalidate(address)
                    protocolCache.invalidate(address)
                    suspicionAdd(address, null)
                    DisconnectType.RECHECK
                }
            }
            val version = info.protocol
            return when (type) {
                RECONNECT -> { cacheJoin(info) }
                JOIN_AFTER_PING -> { if (cachePing(address, false, version)) null else DisconnectType.PING }
                RECONNECT_AFTER_PING -> {
                    if (!cachePing(address, false, version)) { DisconnectType.PING }
                    else cacheJoin(info)
                }
                PING_AFTER_RECONNECT -> {
                    if (cacheJoin(info) == null) { if (cachePing(address, false, version)) null else DisconnectType.PING }
                    else { pingCache.invalidate(address); DisconnectType.REJOIN }
                }
                STABLE -> {
                    val r= cacheJoin(info)
                    r ?: if (!cachePing(address, false, version)) DisconnectType.PING else null
                }
                DISABLED -> { null }
            }
        }
        if (info is Pinging) {
            val address = info.address
            if (Throttler.isThrottled(address)) Firewall.addAddressTemp(address)
            else cachePing(address, true, info.protocol)
            return null
        }
        return null
    }

    private fun cacheJoin(info: CheckInfo): DisconnectType? {
        val joining = info as Joining
        val address = joining.address
        val protocol = info.protocol
        val result = if (joinCache.getIfPresent(address) != null) true else { joinCache.put(address, info); false }
        return if (result) {
            if ((protocolCache.getIfPresent(address) ?: protocol) != protocol || joinCache.getIfPresent(address)!!.username != info.username) {
                joinCache.put(address, info) // Rewrite info but add suspicion point.
                pingCache.invalidate(address)
                protocolCache.invalidate(address)
                suspicionAdd(address, null)
                DisconnectType.RECHECK
            } else null
        } else DisconnectType.REJOIN
    }

    private fun cachePing(address: InetAddress, write: Boolean, version: Int): Boolean {
        //return if (pingCache.getIfPresent(address) != null) true else { if (write) { pingCache.put(address, true); if (protocol != null) protocolCache.put(address, protocol) }; false }
        return if (pingCache.getIfPresent(address) != null && protocolCache.getIfPresent(version) != version) {
            true
        } else {
            if (write) {
                pingCache.put(address, true)
                protocolCache.put(address, version)
            }
            false
        }
    }

    private fun loadType(path: String): MixedType {
        return try { MixedType.valueOf(conf.getAnyRef(path).toString()) }
        catch (ex: ConfigException) { MessageUtil.logError("[MoeFilter] [MixedCheck] Failed to get type. That is empty or config file is outdated?"); DISABLED }
        catch (ex: IllegalArgumentException) { MessageUtil.logWarn("[MoeFilter] [MixedCheck] Unknown mode \"${conf.getAnyRef(path)}\", Disabling.."); DISABLED }
    }

    override fun reload() {
        conf = LocalConfig.getAntibot().getConfig("mixed-check")
        //if (type == DISABLED && this.type == DISABLED) return
        defaultType = loadType("default")
        attackType = loadType("in-attack")
        cache = Caffeine.newBuilder().expireAfterWrite(conf.getLong("max-cache-time"), TimeUnit.SECONDS)
        blacklistSuspicion = conf.getInt("blacklist.suspicion")
        conf.getBoolean("blacklist.only-in-attack")
        val maxCacheTime = conf.getLong("max-cache-time")
        val suspicionDecay = conf.getLong("blacklist.decay")
        if (this.maxCacheTime != maxCacheTime) {
            for (i in listOf(joinCache, pingCache, protocolCache)) i.invalidateAll() // Prevent to memory leak?
            joinCache = cache.build()
            pingCache = cache.build()
            protocolCache = cache.build()
            this.maxCacheTime = maxCacheTime
            MessageUtil.logWarn("[MoeFilter] [MixedCheck] Original mode is not disabled. If someone try to pass checking. They need to do it again.")
        }
        if (this.suspicionDecay != suspicionDecay) {
            this.suspicionDecay = suspicionDecay
            this.suspicionCount = Caffeine.newBuilder()
                .expireAfterWrite(MixedCheck.suspicionDecay, TimeUnit.SECONDS)
                .removalListener { address: InetAddress?, suspicion: Int?, cause: RemovalCause? -> suspicionEvictionListener(address, suspicion, cause) }
                .build<InetAddress, Int>()
            MessageUtil.logWarn("[MoeFilter] [MixedCheck] You changed suspicion decay value. All suspicion level will be clear.")
        }
    }

}