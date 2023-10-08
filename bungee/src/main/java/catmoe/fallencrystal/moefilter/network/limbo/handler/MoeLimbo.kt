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

import catmoe.fallencrystal.moefilter.MoeFilterBungee
import catmoe.fallencrystal.moefilter.common.firewall.lockdown.LockdownManager
import catmoe.fallencrystal.moefilter.listener.main.MainListener
import catmoe.fallencrystal.moefilter.network.bungee.pipeline.MoeChannelHandler
import catmoe.fallencrystal.moefilter.network.bungee.util.WorkingMode
import catmoe.fallencrystal.moefilter.network.limbo.check.falling.MoveCheck
import catmoe.fallencrystal.moefilter.network.limbo.check.falling.MoveTimer
import catmoe.fallencrystal.moefilter.network.limbo.check.impl.ChatCheck
import catmoe.fallencrystal.moefilter.network.limbo.check.impl.CommonJoinCheck
import catmoe.fallencrystal.moefilter.network.limbo.check.impl.KeepAliveTimeout
import catmoe.fallencrystal.moefilter.network.limbo.check.valid.PacketValidCheck
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
import catmoe.fallencrystal.translation.utils.config.IgnoreInitReload
import catmoe.fallencrystal.translation.utils.config.LocalConfig
import catmoe.fallencrystal.translation.utils.config.Reloadable
import net.md_5.bungee.BungeeCord

@Suppress("EnumValuesSoftDeprecate")
@IgnoreInitReload
object MoeLimbo : Reloadable {

    val connections: MutableCollection<LimboHandler> = ArrayList()
    private val rawDimLoaderType = LocalConfig.getLimbo().getAnyRef("dim-loader").toString()
    private val dimension = LocalConfig.getLimbo().getAnyRef("dimension").toString()
    val dimensionType = try {
        CommonDimensionType.valueOf(dimension)
    } catch (_: IllegalArgumentException) {
        MessageUtil.logWarn("[MoeLimbo] Unknown dimension $dimension, Fallback to OVERWORLD.")
        CommonDimensionType.OVERWORLD
    }
    private var conf = LocalConfig.getLimbo()
    var dimLoaderMode = try { DimensionInterface.valueOf(rawDimLoaderType) } catch (_: IllegalArgumentException) {
        MessageUtil.logWarn("[MoeLimbo] Unknown type $rawDimLoaderType, Fallback to LLBIT"); LLBIT
    }
    var debug = LocalConfig.getConfig().getBoolean("debug")
    var useOriginalHandler = LocalConfig.getAntibot().getBoolean("use-original-handler")

    // Debug
    private val disableCheck = LocalConfig.getLimbo().getBoolean("debug.check.disable-all")
    val reduceDebug = LocalConfig.getLimbo().getBoolean("debug.reduce-f3-debug")
    val chunkSent = LocalConfig.getLimbo().getBoolean("debug.chunk.sent")
    val chunkStart = LocalConfig.getLimbo().getInt("debug.chunk.start")
    val chunkLength = chunkStart + LocalConfig.getLimbo().getInt("debug.chunk.length")

    private val checker = listOf(
        CommonJoinCheck,
        MoveCheck,
        MoveTimer,
        KeepAliveTimeout,
        PacketValidCheck,
        ChatCheck,
    )

    override fun reload() {
        if (!LocalConfig.getLimbo().getBoolean("enabled")) return
        LockdownManager.setLockdown(true)
        calibrateConnections()
        if (!disableCheck) checker.forEach { it.unregister() }
        initLimbo()
        LockdownManager.setLockdown(false)
        val useOriginalHandler = LocalConfig.getAntibot().getBoolean("use-original-handler")
        if (this.useOriginalHandler != useOriginalHandler && MoeFilterBungee.mode == WorkingMode.PIPELINE) {
            this.useOriginalHandler=useOriginalHandler
            initEvent()
        }
    }

    private fun initEvent() {
        if (MoeFilterBungee.mode != WorkingMode.PIPELINE) return
        val pm = BungeeCord.getInstance().pluginManager
        when (useOriginalHandler) {
            true -> { pm.registerListener(MoeFilterBungee.instance, MainListener.incomingListener) }
            false -> { pm.unregisterListener(MainListener.incomingListener) }
        }
    }

    fun calibrateConnections() {
        val connections: MutableCollection<LimboHandler> = ArrayList()
        this.connections.forEach { try { if (!it.channel.isActive || MoeChannelHandler.sentHandshake.getIfPresent(it.channel) == null) connections.remove(it) } catch (npe: NullPointerException) { connections.add(it) } }
        this.connections.removeAll(connections.toSet())
    }

    private fun init() {
        conf = LocalConfig.getLimbo()
        dimLoaderMode = try { DimensionInterface.valueOf(rawDimLoaderType) } catch (_: IllegalArgumentException) {
            MessageUtil.logWarn("[MoeLimbo] Unknown type $rawDimLoaderType, Fallback to LLBIT"); LLBIT
        }
        initDimension()
        initEvent()
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

    fun initLimbo() {
        Protocol.values().forEach { Protocol.STATE_BY_ID[it.stateId] = it }
        init()
        if (!disableCheck) for (c in checker) LimboListener.register(c)
        PacketCache.initPacket()
        /*
        val cg = CaptchaGeneration()
        (0..(Runtime.getRuntime().availableProcessors() * 2)).forEach {
            Scheduler(MoeFilter.instance).runAsync { cg.generateImages() }
        }
         */
    }

}