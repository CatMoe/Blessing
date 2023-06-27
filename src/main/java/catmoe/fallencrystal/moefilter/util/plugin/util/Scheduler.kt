package catmoe.fallencrystal.moefilter.util.plugin.util

import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.api.scheduler.ScheduledTask
import java.util.concurrent.TimeUnit

class Scheduler(private val plugin: Plugin) {
    private val scheduler = ProxyServer.getInstance().scheduler
    fun runAsync(run: Runnable): ScheduledTask { return scheduler.runAsync(plugin, run) }

    fun delayScheduler(delay: Long, timeUnit: TimeUnit, run: Runnable): ScheduledTask { return scheduler.schedule(plugin, run, delay, timeUnit) }
    fun repeatScheduler(delay: Long, timeUnit: TimeUnit, run: Runnable): ScheduledTask { return scheduler.schedule(plugin, run, 0, delay, timeUnit) }

    fun repeatScheduler(firstDelay: Long, delay: Long, timeUnit: TimeUnit, run: Runnable): ScheduledTask { return scheduler.schedule(plugin, run, firstDelay, delay, timeUnit) }

    fun cancelTask(taskId: Int) { scheduler.cancel(taskId) }

    fun cancelTask(plugin: Plugin) { scheduler.cancel(plugin) }

    fun cancelTask(task: ScheduledTask) { scheduler.cancel(task) }
}