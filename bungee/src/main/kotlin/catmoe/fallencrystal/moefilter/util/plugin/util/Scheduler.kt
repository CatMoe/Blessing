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

package catmoe.fallencrystal.moefilter.util.plugin.util

import catmoe.fallencrystal.moefilter.MoeFilterBungee
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.api.scheduler.ScheduledTask
import java.util.concurrent.TimeUnit

@Suppress("unused")
class Scheduler(private val plugin: Plugin) {
    private val scheduler = ProxyServer.getInstance().scheduler

    fun runAsync(run: Runnable): ScheduledTask { return scheduler.runAsync(plugin, run) }

    fun delayScheduler(delay: Long, timeUnit: TimeUnit, run: Runnable): ScheduledTask { return scheduler.schedule(plugin, run, delay, timeUnit) }

    fun repeatScheduler(delay: Long, timeUnit: TimeUnit, run: Runnable): ScheduledTask { return scheduler.schedule(plugin, run, 0, delay, timeUnit) }

    fun repeatScheduler(firstDelay: Long, delay: Long, timeUnit: TimeUnit, run: Runnable): ScheduledTask { return scheduler.schedule(plugin, run, firstDelay, delay, timeUnit) }

    fun cancelTask(taskId: Int) { scheduler.cancel(taskId) }

    fun cancelTask(plugin: Plugin) { scheduler.cancel(plugin) }

    fun cancelTask(task: ScheduledTask) { scheduler.cancel(task) }

    companion object {
        fun getDefault(): Scheduler { return Scheduler(MoeFilterBungee.instance) }
    }
}