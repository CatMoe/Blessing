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

package catmoe.fallencrystal.moefilter.network.limbo.handler

import catmoe.fallencrystal.moefilter.common.config.LocalConfig
import catmoe.fallencrystal.moefilter.common.firewall.lockdown.LockdownManager
import catmoe.fallencrystal.moefilter.network.limbo.check.impl.CommonJoinCheck
import catmoe.fallencrystal.moefilter.network.limbo.check.impl.UnexpectedKeepAlive
import catmoe.fallencrystal.moefilter.network.limbo.dimension.CommonDimensionType
import catmoe.fallencrystal.moefilter.network.limbo.dimension.DimensionInterface
import catmoe.fallencrystal.moefilter.network.limbo.dimension.DimensionInterface.ADVENTURE
import catmoe.fallencrystal.moefilter.network.limbo.dimension.DimensionInterface.LLBIT
import catmoe.fallencrystal.moefilter.network.limbo.dimension.adventure.DimensionRegistry
import catmoe.fallencrystal.moefilter.network.limbo.dimension.adventure.DimensionType
import catmoe.fallencrystal.moefilter.network.limbo.dimension.llbit.StaticDimension
import catmoe.fallencrystal.moefilter.network.limbo.listener.LimboListener
import catmoe.fallencrystal.moefilter.network.limbo.packet.cache.PacketCache
import catmoe.fallencrystal.moefilter.network.limbo.packet.protocol.Protocol
import catmoe.fallencrystal.moefilter.util.message.v2.MessageUtil
import com.github.benmanes.caffeine.cache.Caffeine
import java.net.InetAddress
import java.util.concurrent.TimeUnit

object MoeLimbo {

    val connections: MutableCollection<LimboHandler> = ArrayList()
    val dimensionType = CommonDimensionType.OVERWORLD
    private val rawDimType = LocalConfig.getLimbo().getAnyRef("dim-loader").toString()
    private var conf = LocalConfig.getLimbo()
    var bungeeQueue = Caffeine.newBuilder()
        .expireAfterWrite(conf.getLong("bungee-queue"), TimeUnit.SECONDS)
        .build<InetAddress, Boolean>()
    var dimLoaderMode = try { DimensionInterface.valueOf(rawDimType) } catch (_: IllegalArgumentException) {
        MessageUtil.logWarn("[MoeLimbo] Unknown type $rawDimType, Fallback to LLBIT"); LLBIT
    }

    fun reload() {
        LockdownManager.setLockdown(true)
        init()
        LockdownManager.setLockdown(false)
    }

    private fun init() {
        conf = LocalConfig.getLimbo()
        dimLoaderMode = try { DimensionInterface.valueOf(rawDimType) } catch (_: IllegalArgumentException) {
            MessageUtil.logWarn("[MoeLimbo] Unknown type $rawDimType, Fallback to LLBIT"); LLBIT
        }
        initDimension()
        if (!conf.getBoolean("check.unexpected-keepalive.enabled")) { LimboListener.unregister(UnexpectedKeepAlive) }
        bungeeQueue = Caffeine.newBuilder()
            .expireAfterWrite(conf.getLong("bungee-queue"), TimeUnit.SECONDS)
            .build()
    }

    private fun initDimension() {
        when (dimLoaderMode) {
            ADVENTURE -> {
                val dimension = DimensionType.OVERWORLD
                DimensionRegistry.defaultDimension1_16 = DimensionRegistry.getDimension(dimension, DimensionRegistry.codec_1_16)
                DimensionRegistry.defaultDimension1_18_2 = DimensionRegistry.getDimension(dimension, DimensionRegistry.codec_1_18_2)
            }
            LLBIT -> { StaticDimension.init() }
        }
    }

    private fun initCheck() {
        val conf = this.conf.getConfig("check")
        LimboListener.register(CommonJoinCheck)
        if (conf.getBoolean("unexpected-keepalive.enabled")) { LimboListener.register(UnexpectedKeepAlive) }
    }

    fun initLimbo() {
        Protocol.values().forEach { Protocol.STATE_BY_ID[it.stateId] = it }
        init()
        initCheck()
        PacketCache.initPacket()
    }

}