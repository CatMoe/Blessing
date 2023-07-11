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

package catmoe.fallencrystal.moefilter.common.utils.system

import catmoe.fallencrystal.moefilter.common.utils.system.impl.CpuUsage
import com.sun.management.OperatingSystemMXBean
import java.lang.management.ManagementFactory
import java.util.concurrent.*

object CPUMonitor {

    private var latestCpuUsage: CpuUsage = CpuUsage(0.0, 0.0)

    private var executor: ScheduledExecutorService? = null
    private var monitorTask: ScheduledFuture<*>? = null

    fun startSchedule() {
        val ex = ScheduledThreadPoolExecutor(1)
        ex.removeOnCancelPolicy = true
        executor = Executors.unconfigurableScheduledExecutorService(ex)
        monitorTask = executor!!.scheduleAtFixedRate(this::update, 0L, 750L, TimeUnit.MILLISECONDS)
    }

    fun shutdownSchedule() {
        monitorTask!!.cancel(false)
        executor!!.shutdown()
    }

    @Suppress("DEPRECATION")
    private fun update() {
        val osBean = ManagementFactory.getOperatingSystemMXBean() as OperatingSystemMXBean
        val latestCpuUsage = CpuUsage(osBean.processCpuLoad, osBean.systemCpuLoad)
        val zero = 0.000000
        val procCpuUsage = if (latestCpuUsage.processCPU > zero) latestCpuUsage.processCPU else this.latestCpuUsage.processCPU
        val sysCpuUsage = if (latestCpuUsage.systemCPU > zero) latestCpuUsage.systemCPU else this.latestCpuUsage.systemCPU
        if (procCpuUsage > sysCpuUsage) return
        this.latestCpuUsage = CpuUsage(procCpuUsage, sysCpuUsage)
    }

    fun getCpuUsage(): CpuUsage { return latestCpuUsage }

    fun getRoundedCpuUsage(): CpuUsage { return CpuUsage(String.format("%.2f", latestCpuUsage.processCPU * 100).toDouble(), String.format("%.2f", latestCpuUsage.systemCPU * 100).toDouble()) }
}