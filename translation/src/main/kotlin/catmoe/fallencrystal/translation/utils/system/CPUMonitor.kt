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

package catmoe.fallencrystal.translation.utils.system

import catmoe.fallencrystal.translation.utils.system.impl.CpuUsage
import com.sun.management.OperatingSystemMXBean
import java.lang.management.ManagementFactory
import java.util.concurrent.*

@Suppress("unused")
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
        val procCpuUsage = if (latestCpuUsage.processCPU > zero) latestCpuUsage.processCPU else CPUMonitor.latestCpuUsage.processCPU
        val sysCpuUsage = if (latestCpuUsage.systemCPU > zero) latestCpuUsage.systemCPU else CPUMonitor.latestCpuUsage.systemCPU
        if (procCpuUsage > sysCpuUsage) return
        CPUMonitor.latestCpuUsage = CpuUsage(procCpuUsage, sysCpuUsage)
    }

    fun getCpuUsage(): CpuUsage { return latestCpuUsage }

    fun getRoundedCpuUsage(): CpuUsage { return CpuUsage(String.format("%.2f", latestCpuUsage.processCPU * 100).toDouble(), String.format("%.2f", latestCpuUsage.systemCPU * 100).toDouble()) }
}