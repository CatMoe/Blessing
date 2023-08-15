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

package catmoe.fallencrystal.moefilter.common.firewall.system

import catmoe.fallencrystal.moefilter.MoeFilterBungee
import catmoe.fallencrystal.translation.utils.system.CommandUtil
import catmoe.fallencrystal.moefilter.util.message.v2.MessageUtil
import catmoe.fallencrystal.moefilter.util.plugin.util.Scheduler
import com.github.benmanes.caffeine.cache.Caffeine
import net.md_5.bungee.BungeeCord
import net.md_5.bungee.api.scheduler.ScheduledTask
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

@Suppress("unused")
class ExecutorHelper(private val command: String) {
    private val queue: Queue<String> = ArrayDeque()

    val maxSize = AtomicInteger(10000000)
    val debug = AtomicBoolean(false)
    private val idle = AtomicBoolean(true)
    private val watchdog = AtomicBoolean(false)
    private val thread = Runtime.getRuntime().availableProcessors() * 2
    private val count = AtomicInteger(0)
    private val scheduler = Scheduler(MoeFilterBungee.instance)
    private val schedules: MutableCollection<ScheduledTask> = CopyOnWriteArrayList()
    private val threads = AtomicInteger(0)
    private val activeThread = Caffeine.newBuilder().expireAfterWrite(30, TimeUnit.MILLISECONDS).build<ScheduledTask, Boolean>()
    private val intThread = Caffeine.newBuilder().build<Int, ScheduledTask>()

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
        scheduler.repeatScheduler(1, TimeUnit.SECONDS) {
            schedules.forEach { if (activeThread.getIfPresent(it) == null) { schedules.remove(it) } }
            if (!watchdog.get()) return@repeatScheduler
            if (queue.size == 0 || count.get() == 0) idle.set(true)
            if (debug.get() && !idle.get()) { MessageUtil.logInfo("[MoeFilter] [ExecutorHelper] Queue: ${queue.size} Threads: ${count.get()}") }
        }
    }

    @Suppress("UNUSED_VARIABLE")
    private fun schedule() {
        if (count.get() >= thread) return
        count.set(count.get() + 1)
        val threads = this.threads.get() + 1
        this.threads.set(threads)
        val schedule = scheduler.repeatScheduler(10, TimeUnit.MILLISECONDS) {
            val task = intThread.getIfPresent(threads)
            if (task != null) {
                activeThread.put(task, true); schedules.add(task)
                if (queue.isEmpty()) { count.set(count.get() - 1); activeThread.invalidate(task); return@repeatScheduler }
                val command = this.command.replace("{chain}", queue.poll())
                // Can use reflect to edit this.
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