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

package catmoe.fallencrystal.moefilter.common.state

import catmoe.fallencrystal.moefilter.common.counter.ConnectionStatistics
import catmoe.fallencrystal.moefilter.common.firewall.lockdown.LockdownManager
import catmoe.fallencrystal.moefilter.data.BlockType
import catmoe.fallencrystal.moefilter.data.ticking.TickingAttackProfile
import catmoe.fallencrystal.moefilter.data.ticking.TickingProfile
import catmoe.fallencrystal.moefilter.event.AttackStoppedEvent
import catmoe.fallencrystal.moefilter.event.UnderAttackEvent
import catmoe.fallencrystal.moefilter.state.AttackState
import catmoe.fallencrystal.moefilter.util.plugin.util.Scheduler
import catmoe.fallencrystal.translation.event.EventManager
import catmoe.fallencrystal.translation.utils.config.LocalConfig
import catmoe.fallencrystal.translation.utils.system.CPUMonitor
import catmoe.fallencrystal.translation.utils.time.Duration
import net.md_5.bungee.api.scheduler.ScheduledTask
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

@Suppress("MemberVisibilityCanBePrivate")
object StateManager {

    val inAttack = AtomicBoolean(false)
    var attackMethods: MutableCollection<AttackState> = CopyOnWriteArrayList()
    val duration = Duration()
    var profile: TickingProfile? = null

    private val attackEndedWaiter = AtomicBoolean(false)
    private val attackEndedCount = AtomicInteger(0)

    val lastMethod: MutableCollection<AttackState> = ArrayList()

    private val scheduler = Scheduler.getDefault()

    @Suppress("EnumValuesSoftDeprecate")
    fun attackMethodAnalyser() {
        if (!inAttack.get()) return
        val conf = LocalConfig.getAntibot().getConfig("attack-mode")
        val cps = ConnectionStatistics.getConnectionPerSec()
        val inc = conf.getInt("incoming")
        if (cps < inc && attackMethods.isNotEmpty()) return
        val methodSize = AttackState.values().size
        if (cps >= methodSize) {
            val method: MutableCollection<AttackState> = ArrayList()
            if (cps > methodSize * 2) {
                BlockType.values().forEach {
                    if ((ConnectionStatistics.sessionBlocked.getIfPresent(it) ?: 0) > cps / methodSize) { method.add(it.state) }
                }
                if (method == lastMethod) return
                lastMethod.clear(); lastMethod.addAll(method)
                setAttackMethod(method); return
            }
        }
    }

    fun attackEndedDetector() {
        val conf = LocalConfig.getAntibot().getConfig("attack-mode.un-attacked")
        val cps = ConnectionStatistics.getConnectionPerSec()
        if (inAttack.get() && cps == 0) {
            if (conf.getBoolean("instant")) { fireNotInAttackEvent() }
            else {
                if (!attackEndedWaiter.get()) {
                    attackEndedWaiter.set(true)
                    var task: ScheduledTask? = null
                    task = scheduler.repeatScheduler(1, 1, TimeUnit.SECONDS) {
                        if (!attackEndedWaiter.get()) { attackEndedCount.set(conf.getInt("wait") + 1); scheduler.cancelTask(task!!) }
                        if (ConnectionStatistics.getConnectionPerSec() != 0) {
                            attackEndedCount.set(conf.getInt("wait"))
                            attackEndedWaiter.set(false); scheduler.cancelTask(task!!)
                        }
                        val c = attackEndedCount.get()
                        if (c == 0) { fireNotInAttackEvent(); attackEndedWaiter.set(false); scheduler.cancelTask(task!!) }
                        attackEndedCount.set(c - 1)
                    }
                }
            }
        }
    }

    fun setAttackMethod(method: Collection<AttackState>) {
        if (!inAttack.get()) return
        attackMethods.clear(); attackMethods.addAll(method)
        EventManager.callEvent(UnderAttackEvent(attackMethods))
    }

    fun fireAttackEvent() {
        EventManager.callEvent(UnderAttackEvent(
            if (LockdownManager.state.get()) listOf(AttackState.LOCKDOWN)
            else if (attackMethods.isEmpty()) listOf(AttackState.NOT_HANDLED)
            else attackMethods
        ))
        inAttack.set(true)
        if (inAttack.get()) { duration.start() }
    }

    fun fireNotInAttackEvent() { EventManager.callEvent(AttackStoppedEvent()); attackMethods.clear(); inAttack.set(false); duration.stop() }

    fun tickProfile(): TickingProfile {
        val attackProfile = if (inAttack.get()) {
            TickingAttackProfile(
                ConnectionStatistics.sessionPeakCps,
                ConnectionStatistics.sessionBlocked.asMap().toMutableMap(),
                ConnectionStatistics.sessionTotal,
                ConnectionStatistics.sessionTotalIps
            )
        } else { null }
        val profile = TickingProfile(
            attackProfile,
            ConnectionStatistics.getConnectionPerSec(),
            ConnectionStatistics.peakCps,
            ConnectionStatistics.total,
            ConnectionStatistics.totalIps,
            ConnectionStatistics.blocked.asMap().toMutableMap(),
            CPUMonitor.getRoundedCpuUsage(),
            ConnectionStatistics.getIncoming(),
            ConnectionStatistics.getOutgoing()
        )
        this.profile=profile
        return profile
    }

}