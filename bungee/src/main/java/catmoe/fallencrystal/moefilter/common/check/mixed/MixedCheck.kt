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
import catmoe.fallencrystal.translation.utils.config.LocalConfig
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.RemovalCause
import com.typesafe.config.ConfigException
import java.net.InetAddress
import java.util.concurrent.TimeUnit

object MixedCheck {

    private var conf = LocalConfig.getAntibot().getConfig("mixed-check")
    private var maxCacheTime = conf.getLong("max-cache-time")
    private var cache = Caffeine.newBuilder().expireAfterWrite(maxCacheTime, TimeUnit.SECONDS)

    private var suspicionDecay = conf.getLong("blacklist.decay")
    private var blacklistSuspicion = conf.getInt("blacklist.suspicion")
    private var suspicionOnlyInAttack = conf.getBoolean("blacklist.only-in-attack")

    private val protocolCache = Caffeine.newBuilder().build<InetAddress, Int>()

    private var joinCache = cache.build<InetAddress, Joining>()
    private var pingCache = cache.build<InetAddress, Boolean>()

    private var suspicionCount = Caffeine.newBuilder()
        .expireAfterWrite(suspicionDecay, TimeUnit.SECONDS)
        .removalListener { address: InetAddress?, suspicion: Int?, cause: RemovalCause? -> suspicionEvictionListener(address, suspicion, cause) }
        .build<InetAddress, Int>()

    private fun suspicionEvictionListener(address: InetAddress?, suspicion: Int?, cause: RemovalCause?) {
        if (suspicion == null || suspicion == 1 || address == null || cause != RemovalCause.EXPIRED) return
        if (suspicion >= blacklistSuspicion) {
            if (suspicionOnlyInAttack) { if (StateManager.inAttack.get()) Firewall.addAddressTemp(address) }
            else { Firewall.addAddressTemp(address) }; suspicionCount.invalidate(address); return }
        suspicionAdd(address, suspicion - 1)
    }

    private var type: MixedType = loadType()

    private fun suspicionAdd(address: InetAddress, suspicion: Int?) {
        val s = suspicion ?: (suspicionCount.getIfPresent(address)?.plus(1)) ?: 1
        suspicionCount.put(address, s)
    }

    fun increase(info: CheckInfo): DisconnectType? {
        if (info is Joining) {
            val address = info.address
            // if (MoeLimbo.bungeeQueue.getIfPresent(address) != null) return null
            if (BungeeSwitcher.connectToBungee(address) && BungeeSwitcher.limbo) {
                return if (BungeeSwitcher.verify(info)) null else {
                    joinCache.put(address, info) // Rewrite info but add suspicion point.
                    pingCache.invalidate(address)
                    protocolCache.invalidate(address)
                    suspicionAdd(address, null)
                    DisconnectType.RECHECK
                }
            }
            return when (type) {
                RECONNECT -> { cacheJoin(info) }
                JOIN_AFTER_PING -> { if (cachePing(address, false)) null else DisconnectType.PING }
                RECONNECT_AFTER_PING -> {
                    if (!cachePing(address, false)) { DisconnectType.PING }
                    else cacheJoin(info)
                }
                PING_AFTER_RECONNECT -> {
                    if (cacheJoin(info) == null) { if (cachePing(address, false)) null else DisconnectType.PING }
                    else { pingCache.invalidate(address); DisconnectType.REJOIN }
                }
                STABLE -> {
                    val r= cacheJoin(info)
                    r ?: if (!cachePing(address, false)) DisconnectType.PING else null
                }
                DISABLED -> { null }
            }
        }
        if (info is Pinging) {
            val address = info.address
            if (Throttler.isThrottled(address)) { Firewall.addAddressTemp(address) }
            else { cachePing(address, true); protocolCache.put(address, info.protocol) }
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

    private fun cachePing(address: InetAddress, write: Boolean): Boolean {
        return if (pingCache.getIfPresent(address) != null) true else { if (write) { pingCache.put(address, true) }; false }
    }

    private fun loadType(): MixedType {
        return try { MixedType.valueOf(conf.getAnyRef("join-ping-mixin-mode").toString()) }
        catch (ex: ConfigException) { MessageUtil.logError("[MoeFilter] [MixedCheck] Failed to get type. That is empty or config file is outdated?"); DISABLED }
        catch (ex: IllegalArgumentException) { MessageUtil.logWarn("[MoeFilter] [MixedCheck] Unknown mode \"${conf.getAnyRef("join-ping-mixin-mode")}\", Disabling.."); DISABLED }
    }

    fun reload() {
        conf = LocalConfig.getAntibot().getConfig("mixed-check")
        val type = loadType()
        if (type == DISABLED && this.type == DISABLED) return
        this.type = type
        cache = Caffeine.newBuilder().expireAfterWrite(conf.getLong("max-cache-time"), TimeUnit.SECONDS)
        blacklistSuspicion = conf.getInt("blacklist.suspicion")
        conf.getBoolean("blacklist.only-in-attack")
        val maxCacheTime = conf.getLong("max-cache-time")
        val suspicionDecay = conf.getLong("blacklist.decay")
        if (this.maxCacheTime != maxCacheTime) {
            joinCache = cache.build()
            pingCache = cache.build()
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
