package catmoe.fallencrystal.moefilter.util.system

import catmoe.fallencrystal.moefilter.util.message.MessageUtil
import catmoe.fallencrystal.moefilter.util.plugin.FilterPlugin
import catmoe.fallencrystal.moefilter.util.system.impl.CPUUsage
import com.sun.management.OperatingSystemMXBean
import net.md_5.bungee.api.ProxyServer
import java.lang.management.ManagementFactory
import java.util.concurrent.TimeUnit

object CPUMonitor {
    private val runtime = Runtime.getRuntime()

    /*
    Beans Here
     */
    private val osBean = ManagementFactory.getOperatingSystemMXBean() as OperatingSystemMXBean

    private val plugin = FilterPlugin.getPlugin()

    private var detectCPUUsage = true
    private var latestCPUUsage: CPUUsage = CPUUsage(0.0, 0.0)

    init { update() }

    @Suppress("DEPRECATION")
    private fun update() {
        ProxyServer.getInstance().scheduler.schedule(plugin, {
            try {
                if (detectCPUUsage) { latestCPUUsage = CPUUsage(osBean.processCpuLoad, osBean.systemCpuLoad) }
            } catch (ex: Exception) { ex.printStackTrace(); MessageUtil.logWarn("CPU Usage is not available on your services"); detectCPUUsage = false }
            }, 1L, TimeUnit.SECONDS)
    }

    fun getCPUUsage(): CPUUsage { return latestCPUUsage }

    fun getRoundedCPUUsage(): CPUUsage { return CPUUsage(String.format("%.1f", latestCPUUsage.processCPU).toDouble(), String.format("%.1f", latestCPUUsage.systemCPU).toDouble()) }
}