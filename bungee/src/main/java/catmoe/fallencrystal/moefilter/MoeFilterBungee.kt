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

package catmoe.fallencrystal.moefilter

import catmoe.fallencrystal.moefilter.api.logger.InitLogger
import catmoe.fallencrystal.moefilter.api.proxy.ProxyCache
import catmoe.fallencrystal.moefilter.common.firewall.Firewall
import catmoe.fallencrystal.moefilter.common.firewall.lockdown.LockdownManager
import catmoe.fallencrystal.moefilter.common.geoip.GeoIPManager
import catmoe.fallencrystal.moefilter.event.PluginUnloadEvent
import catmoe.fallencrystal.moefilter.network.bungee.util.WorkingMode
import catmoe.fallencrystal.moefilter.network.limbo.handler.MoeLimbo
import catmoe.fallencrystal.moefilter.util.message.v2.MessageUtil
import catmoe.fallencrystal.moefilter.util.plugin.AsyncLoader
import catmoe.fallencrystal.moefilter.util.plugin.util.Scheduler
import catmoe.fallencrystal.translation.CPlatform
import catmoe.fallencrystal.translation.event.EventManager
import catmoe.fallencrystal.translation.platform.*
import catmoe.fallencrystal.translation.utils.system.CPUMonitor
import net.md_5.bungee.BungeeCord
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.plugin.Plugin
import java.io.File
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.logging.Level

@Platform(ProxyPlatform.BUNGEE)
class MoeFilterBungee : Plugin(), PlatformLoader {

    private val initLogger = InitLogger()

    private val viaLoader = CPlatform(this)

    init {
        instance=this
        viaLoader.readyLoad()
    }

    override fun onEnable() {
        load()
    }

    override fun onDisable() {
        viaLoader.whenUnload()
        initLogger.onUnload()
        EventManager.callEvent(PluginUnloadEvent())
    }

    private fun load() {
        val loader = AsyncLoader(this, viaLoader)
        initLogger.onLoad()
        loader.load()
        memoryLeak()
    }

    override fun onLoad() {
        MessageUtil.logInfo("[MoeFilter] Using MoeFilter API")
    }

    companion object {
        lateinit var instance: MoeFilterBungee
            private set
        var mode: WorkingMode? = null
    }

    private val detectorRunning = AtomicBoolean(false)

    private fun memoryLeak() {
        if (detectorRunning.get()) return else detectorRunning.set(true)
        val time = AtomicLong(System.currentTimeMillis())
        val detected = AtomicBoolean(false)
        val step = AtomicInteger(0)
        Scheduler(this).repeatScheduler(1, 1, TimeUnit.SECONDS) {
            val cpu = CPUMonitor.getCpuUsage()
            val now = System.currentTimeMillis()
            if (now - time.get() <= 1050 && cpu.processCPU < 0.5 && detected.get()) {
                logger.log(Level.WARNING, "[Memory Leak Detector] Memory leak may solved.")
                detected.set(false)
            }
            if (now - time.get() > 1500 || cpu.processCPU > 0.5 && (cpu.systemCPU / 2) < cpu.processCPU) {
                logger.log(Level.WARNING, "[Memory Leak Detector] Memory leak may detected.")
                detected.set(true)
                step.set(1)
            }
            time.set(now)
            if (!detected.get()) step.set(0) else {
                when (step.get()) {
                    1 -> { ProxyCache.cache.invalidateAll(); logger.log(Level.WARNING, "[Memory Leak Detector] Trying step 1: Invalidate proxy cache") }
                    2 -> { Firewall.executor.shutdown(); logger.log(Level.WARNING, "[Memory Leak Detector] Trying step 2: Shutdown bash executor helper") }
                    3 -> { Firewall.cache.invalidateAll(); logger.log(Level.WARNING, "[Memory Leak Detector] Trying step 3: Drop all never expire firewalled address") }
                    4 -> {
                        logger.log(Level.WARNING, "[Memory Leak Detector] Trying step 4: Disconnect all connect from limbo")
                        LockdownManager.setLockdown(true)
                        while (MoeLimbo.connections.isNotEmpty()) {
                            for (i in MoeLimbo.connections) { i.channel.close() }
                            MoeLimbo.connections.clear()
                        }
                        LockdownManager.setLockdown(false)
                    }
                    5 -> {
                        logger.log(Level.WARNING, "[Memory Leak Detector] Trying step 5: Re-setup country database")
                        if (GeoIPManager.available.get()) {
                            GeoIPManager.country=null
                            GeoIPManager.city=null
                            System.gc()
                            AsyncLoader.instance.geoIPLoader?.update()
                        } else {
                            logger.log(Level.WARNING, "[Memory Leak Detector] GeoIP is not installed.")
                        }
                    }
                }
                step.set(step.get() + 1)
            }
        }
    }

    override fun getPluginFolder(): File {
        return dataFolder
    }

    override fun getPluginInstance(): PlatformLoader {
        return instance
    }

    override fun whenLoad() {  }

    override fun whenUnload() {  }

    override fun pluginVersion(): String {
        return this.description.version
    }

    override fun readyLoad() {  }

    override fun getPlatformLogger(): SimpleLogger {
        return SimpleLogger(BungeeCord.getInstance().logger)
    }

    override fun getProxyServer(): MoeProxyServer {
        val proxy = ProxyServer.getInstance()
        return MoeProxyServer(ProxyPlatform.BUNGEE, proxy)
    }
}