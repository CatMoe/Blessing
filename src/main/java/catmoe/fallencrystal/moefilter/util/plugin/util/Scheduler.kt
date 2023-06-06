package catmoe.fallencrystal.moefilter.util.plugin.util

import catmoe.fallencrystal.moefilter.util.plugin.FilterPlugin
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.api.scheduler.ScheduledTask
import java.util.concurrent.TimeUnit

class Scheduler {
    private val scheduler = ProxyServer.getInstance().scheduler
    private val plugin = FilterPlugin.getPlugin()
    fun runAsync(run: Runnable): ScheduledTask { return scheduler.runAsync(plugin, run) }
    fun runAsync(plugin: Plugin, run: Runnable): ScheduledTask { return scheduler.runAsync(plugin, run) }

    fun delayScheduler(delay: Long, timeUnit: TimeUnit, run: Runnable): ScheduledTask { return scheduler.schedule(plugin, run, delay, timeUnit) }
    fun delayScheduler(plugin: Plugin, delay: Long, timeUnit: TimeUnit, run: Runnable): ScheduledTask { return scheduler.schedule(plugin, run, delay, timeUnit) }
    fun repeatScheduler(delay: Long, timeUnit: TimeUnit, run: Runnable): ScheduledTask { return scheduler.schedule(plugin, run, 0, delay, timeUnit) }
    fun repeatScheduler(plugin: Plugin, delay: Long, timeUnit: TimeUnit, run: Runnable): ScheduledTask { return scheduler.schedule(plugin, run, 0, delay, timeUnit) }
    fun repeatScheduler(firstDelay: Long, delay: Long, timeUnit: TimeUnit, run: Runnable): ScheduledTask { return scheduler.schedule(plugin, run, firstDelay, delay, timeUnit) }
    fun repeatScheduler(plugin: Plugin, firstDelay: Long, delay: Long, timeUnit: TimeUnit, run: Runnable): ScheduledTask { return scheduler.schedule(plugin, run, firstDelay, delay, timeUnit) }
}