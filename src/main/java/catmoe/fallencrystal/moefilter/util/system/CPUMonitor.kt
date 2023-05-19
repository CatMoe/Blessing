package catmoe.fallencrystal.moefilter.util.system

import catmoe.fallencrystal.moefilter.util.plugin.FilterPlugin
import com.sun.management.OperatingSystemMXBean
import net.md_5.bungee.api.ProxyServer
import java.lang.management.ManagementFactory
import java.util.concurrent.TimeUnit

object CPUMonitor {
    private val osBean = ManagementFactory.getOperatingSystemMXBean() as OperatingSystemMXBean
    private val plugin = FilterPlugin.getPlugin()

    private var processCPU = 0.0
    private var systemCPU = 0.0

    init { update() }

    @Suppress("DEPRECATION")
    private fun update() {
        try {
            ProxyServer.getInstance().scheduler.schedule(plugin, {
                processCPU = osBean.processCpuLoad
                systemCPU = osBean.systemCpuLoad
            }, 1L, TimeUnit.SECONDS)
        } catch (ex: Exception) { throw RuntimeException("CPUMonitor is not available on your device.") }
    }

    fun getProcessUsage(): Double { return processCPU }
    fun getSystemUsage(): Double { return systemCPU }

    fun getRoundedProcessUsage(): Double { return String.format("%.1f", processCPU).toDouble() }
    fun getRoundedSystemUsage(): Double { return String.format("%.1f", systemCPU).toDouble() }
}