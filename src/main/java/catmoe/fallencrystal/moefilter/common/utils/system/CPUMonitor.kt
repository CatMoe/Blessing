package catmoe.fallencrystal.moefilter.common.utils.system

import catmoe.fallencrystal.moefilter.common.utils.system.impl.CpuUsage
import catmoe.fallencrystal.moefilter.util.message.MessageUtil
import catmoe.fallencrystal.moefilter.util.plugin.FilterPlugin
import catmoe.fallencrystal.moefilter.util.plugin.util.Scheduler
import com.sun.management.OperatingSystemMXBean
import java.lang.management.ManagementFactory
import java.util.concurrent.TimeUnit

object CPUMonitor {
    private val runtime = Runtime.getRuntime()

    /*
    Beans Here
     */
    private val osBean = ManagementFactory.getOperatingSystemMXBean() as OperatingSystemMXBean

    private var detectCPUUsage = true
    private var latestCPUUsage: CpuUsage = CpuUsage(0.0, 0.0)

    init { update() }

    @Suppress("DEPRECATION")
    private fun update() {
        Scheduler(FilterPlugin.getPlugin()!!).repeatScheduler(1, TimeUnit.SECONDS) {
            try { if (detectCPUUsage) { latestCPUUsage = CpuUsage(osBean.processCpuLoad, osBean.systemCpuLoad) }
            } catch (ex: Exception) { ex.printStackTrace(); MessageUtil.logWarn("CPU Usage is not available on your services"); detectCPUUsage = false; return@repeatScheduler }
        }
    }

    fun getCpuUsage(): CpuUsage { return latestCPUUsage }

    fun getRoundedCpuUsage(): CpuUsage { return CpuUsage(String.format("%.2f", latestCPUUsage.processCPU * 100).toDouble(), String.format("%.2f", latestCPUUsage.systemCPU * 100).toDouble()) }
}