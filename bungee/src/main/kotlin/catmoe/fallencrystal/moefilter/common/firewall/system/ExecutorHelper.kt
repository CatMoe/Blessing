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

package catmoe.fallencrystal.moefilter.common.firewall.system

import catmoe.fallencrystal.moefilter.util.message.v2.MessageUtil
import catmoe.fallencrystal.moefilter.util.plugin.util.Scheduler
import catmoe.fallencrystal.translation.utils.system.CommandUtil
import com.github.benmanes.caffeine.cache.Caffeine
import net.md_5.bungee.BungeeCord
import net.md_5.bungee.api.scheduler.ScheduledTask
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

@Suppress("unused", "MemberVisibilityCanBePrivate")
class ExecutorHelper(private val command: String) {
    private val queue: Queue<String> = ArrayDeque()

    val maxSize = AtomicInteger(1000)
    val debug = AtomicBoolean(false)
    val idle = AtomicBoolean(true)
    val watchdog = AtomicBoolean(false)
    val thread = Runtime.getRuntime().availableProcessors() * 2
    val count = AtomicInteger(0)
    val scheduler = Scheduler.getDefault()
    val schedules: MutableCollection<ScheduledTask> = CopyOnWriteArrayList()
    val threads = AtomicInteger(0)
    val activeThread = Caffeine.newBuilder().expireAfterWrite(30, TimeUnit.MILLISECONDS).build<ScheduledTask, Boolean>()
    val intThread = Caffeine.newBuilder().build<Int, ScheduledTask>()

    private fun init(process: Int) {
        val taskCount = (if (process >= thread) thread else process) - count.get()
        if (taskCount != 0) { (1..taskCount).forEach { _ -> schedule() } }
        if (!watchdog.get()) { watchDog() }
    }

    fun shutdown() {
        watchdog.set(false); schedules.forEach { it.cancel(); activeThread.invalidate(it) }
        count.set(0); threads.set(0); intThread.invalidateAll()
    }

    private fun watchDog() {
        var task: ScheduledTask? = null
        task = scheduler.repeatScheduler(1, TimeUnit.SECONDS) {
            schedules.forEach { if (activeThread.getIfPresent(it) == null) { schedules.remove(it) } }
            val t = task
            if (!watchdog.get() && t != null) t.cancel()
            if (queue.isEmpty() || count.get() == 0) idle.set(true)
            if (debug.get() && !idle.get()) { MessageUtil.logInfo("[MoeFilter] [ExecutorHelper] Queue: ${queue.size} Threads: ${count.get()}") }
        }
    }

    @Suppress("UNUSED_VARIABLE")
    private fun schedule() {
        if (count.get() >= thread) return
        count.set(count.get() + 1)
        val threads = this.threads.get() + 1
        this.threads.set(threads)
        if (!watchdog.get()) this.watchDog()
        var schedule: ScheduledTask? = null
        schedule = scheduler.repeatScheduler(5, TimeUnit.MILLISECONDS) {
            val task = intThread.getIfPresent(threads)
            if (task != null) {
                activeThread.put(task, true); schedules.add(task)
                if (queue.isEmpty()) { count.set(count.get() - 1); activeThread.invalidate(task); scheduler.cancelTask(schedule!!) }
                val command = this.command.replace("{chain}", queue.poll())
                // Can use reflection to edit this.
                val unit = execute(command)
            }
        }
        schedules.add(schedule)
    }

    private fun execute(command: String) { try { CommandUtil.execute(command) } catch (ex: Exception) { ex.printStackTrace(); shutdown(); BungeeCord.getInstance().stop() } }

    fun addToQueue(string: String) {
        val queue = this.queue.size + 1
        if (queue >= maxSize.get()) throw IndexOutOfBoundsException("Queue size is $queue but max size is ${maxSize.get()}")
        this.queue.add(string)
        if (queue >= count.get()) this.init(queue)
        idle.set(false)
    }

    fun addToQueue(string: List<String>) {
        val queue = this.queue.size + 1
        if (queue >= maxSize.get()) throw IndexOutOfBoundsException("Queue size is $queue but max size is ${maxSize.get()}")
        this.queue.addAll(string)
        if (queue > count.get()) this.init(queue)
        idle.set(false)
    }

    fun getScheduler(): Collection<ScheduledTask> { return schedules }

    fun getScheduler(taskId: Int): ScheduledTask? { schedules.forEach { if (it.id==taskId) return it }; return null }

}