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

import catmoe.fallencrystal.moefilter.common.check.info.CheckInfo
import catmoe.fallencrystal.moefilter.common.check.info.impl.Joining
import catmoe.fallencrystal.moefilter.common.check.info.impl.Pinging
import catmoe.fallencrystal.moefilter.common.check.mixed.MixedType.*
import catmoe.fallencrystal.moefilter.common.config.LocalConfig
import catmoe.fallencrystal.moefilter.listener.firewall.FirewallCache
import catmoe.fallencrystal.moefilter.listener.firewall.Throttler
import catmoe.fallencrystal.moefilter.network.bungee.util.kick.DisconnectType
import catmoe.fallencrystal.moefilter.util.message.v2.MessageUtil
import com.github.benmanes.caffeine.cache.Caffeine
import com.typesafe.config.ConfigException
import java.net.InetAddress
import java.util.concurrent.TimeUnit

object MixedCheck {

    private var conf = LocalConfig.getAntibot().getConfig("general")
    private var maxCacheTime = conf.getLong("max-cache-time")
    private var cache = Caffeine.newBuilder().expireAfterWrite(maxCacheTime, TimeUnit.SECONDS)

    private var joinCache = cache.build<InetAddress, String>()
    private var pingCache = cache.build<InetAddress, Boolean>()
    private var type: MixedType = loadType()

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

    private fun loadType(): MixedType {
        return try { MixedType.valueOf(conf.getAnyRef("join-ping-mixin-mode").toString()) }
        catch (ex: ConfigException) { MessageUtil.logError("[MoeFilter] [MixedCheck] Failed to get type. That is empty or config file is outdated?"); DISABLED }
        catch (ex: IllegalArgumentException) { MessageUtil.logWarn("[MoeFilter] [MixedCheck] Unknown mode \"${conf.getAnyRef("join-ping-mixin-mode")}\", Disabling.."); DISABLED }
    }

    fun reload() {
        conf = LocalConfig.getAntibot().getConfig("general")
        val type = loadType()
        if (type == DISABLED && this.type == DISABLED) return
        this.type = type
        cache = Caffeine.newBuilder().expireAfterWrite(conf.getLong("max-cache-time"), TimeUnit.SECONDS)
        val maxCacheTime = conf.getLong("max-cache-time")
        if (this.maxCacheTime != maxCacheTime) {
            joinCache = cache.build()
            pingCache = cache.build()
            this.maxCacheTime = maxCacheTime
            MessageUtil.logWarn("[MoeFilter] [MixedCheck] Original mode is not disabled. If someone try to pass checking. They need to do it again.")
        }
    }

}
