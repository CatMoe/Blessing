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
import catmoe.fallencrystal.moefilter.network.limbo.check.falling.FallingCheck
import catmoe.fallencrystal.moefilter.network.limbo.check.impl.CommonJoinCheck
import catmoe.fallencrystal.moefilter.network.limbo.check.impl.InstantDisconnectCheck
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
import java.util.concurrent.TimeUnit

object MoeLimbo {

    val connections: MutableCollection<LimboHandler> = ArrayList()
    val dimensionType = CommonDimensionType.OVERWORLD
    private val rawDimType = LocalConfig.getLimbo().getAnyRef("dim-loader").toString()
    private var conf = LocalConfig.getLimbo()
    var dimLoaderMode = try { DimensionInterface.valueOf(rawDimType) } catch (_: IllegalArgumentException) {
        MessageUtil.logWarn("[MoeLimbo] Unknown type $rawDimType, Fallback to LLBIT"); LLBIT
    }
    var debug = LocalConfig.getConfig().getBoolean("debug")

    val sentHandshake = Caffeine.newBuilder()
        .expireAfterWrite(30, TimeUnit.SECONDS)
        .build<LimboHandler, Boolean>()

    fun reload() {
        LockdownManager.setLockdown(true)
        calibrateConnections()
        init()
        LockdownManager.setLockdown(false)
    }

    fun calibrateConnections() {
        val connections: MutableCollection<LimboHandler> = ArrayList()
        this.connections.forEach { try { it.channel } catch (npe: NullPointerException) { connections.add(it) } }
        this.connections.removeAll(connections.toSet())
    }

    private fun init() {
        conf = LocalConfig.getLimbo()
        dimLoaderMode = try { DimensionInterface.valueOf(rawDimType) } catch (_: IllegalArgumentException) {
            MessageUtil.logWarn("[MoeLimbo] Unknown type $rawDimType, Fallback to LLBIT"); LLBIT
        }
        initDimension()
        if (!conf.getBoolean("check.unexpected-keepalive.enabled")) { LimboListener.unregister(UnexpectedKeepAlive) }
        debug = LocalConfig.getConfig().getBoolean("debug")
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

    fun debug(log: String) {
        if (debug) MessageUtil.logInfo("[MoeLimbo] $log")
    }

    private fun initCheck() {
        LimboListener.register(CommonJoinCheck)
        LimboListener.register(FallingCheck)
        // if (conf.getBoolean("unexpected-keepalive.enabled")) { LimboListener.register(UnexpectedKeepAlive) }
        LimboListener.register(InstantDisconnectCheck)
    }

    fun initLimbo() {
        Protocol.values().forEach { Protocol.STATE_BY_ID[it.stateId] = it }
        init()
        initCheck()
        PacketCache.initPacket()
        /*
        val cg = CaptchaGeneration()
        (0..(Runtime.getRuntime().availableProcessors() * 2)).forEach {
            Scheduler(MoeFilter.instance).runAsync { cg.generateImages() }
        }
         */
    }

}