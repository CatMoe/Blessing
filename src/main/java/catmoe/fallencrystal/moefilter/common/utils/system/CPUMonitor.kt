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