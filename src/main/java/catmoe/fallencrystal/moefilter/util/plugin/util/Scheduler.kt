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