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

package catmoe.fallencrystal.moefilter.common.state

import catmoe.fallencrystal.moefilter.MoeFilter
import catmoe.fallencrystal.moefilter.api.event.EventManager
import catmoe.fallencrystal.moefilter.api.event.events.AttackStoppedEvent
import catmoe.fallencrystal.moefilter.api.event.events.UnderAttackEvent
import catmoe.fallencrystal.moefilter.common.config.LocalConfig
import catmoe.fallencrystal.moefilter.common.counter.ConnectionCounter
import catmoe.fallencrystal.moefilter.common.counter.type.BlockType
import catmoe.fallencrystal.moefilter.common.firewall.lockdown.LockdownManager
import catmoe.fallencrystal.moefilter.util.plugin.util.Scheduler
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

@Suppress("MemberVisibilityCanBePrivate")
object StateManager {

    val inAttack = AtomicBoolean(false)
    var attackMethods: MutableCollection<AttackState> = CopyOnWriteArrayList()
    val duration = AttackDuration()

    private val attackEndedWaiter = AtomicBoolean(false)
    private val attackEndedCount = AtomicInteger(0)

    val lastMethod: MutableCollection<AttackState> = ArrayList()

    fun attackMethodAnalyser() {
        if (!inAttack.get()) return
        val conf = LocalConfig.getAntibot().getConfig("attack-mode")
        val cps = ConnectionCounter.getConnectionPerSec()
        val inc = conf.getInt("incoming")
        if (cps < inc && attackMethods.isNotEmpty()) return
        val methodSize = AttackState.values().size
        if (cps >= methodSize) {
            val method: MutableCollection<AttackState> = ArrayList()
            if (cps > methodSize * 2) {
                BlockType.values().forEach { if ((ConnectionCounter.sessionBlocked[it] ?: 0) > cps / methodSize) { method.add(it.state) } }
                if (method == lastMethod) return
                lastMethod.clear(); lastMethod.addAll(method)
                setAttackMethod(method); return
            }
        }
    }

    fun attackEndedDetector() {
        val conf = LocalConfig.getAntibot().getConfig("attack-mode.un-attacked")
        val cps = ConnectionCounter.getConnectionPerSec()
        if (inAttack.get() && cps == 0) {
            if (conf.getBoolean("instant")) { fireNotInAttackEvent() }
            else {
                if (!attackEndedWaiter.get()) {
                    attackEndedWaiter.set(true)
                    Scheduler(MoeFilter.instance).repeatScheduler(1, 1, TimeUnit.SECONDS) {
                        if (!attackEndedWaiter.get()) { attackEndedCount.set(conf.getInt("wait") + 1); return@repeatScheduler }
                        if (ConnectionCounter.getConnectionPerSec() != 0) {
                            attackEndedCount.set(conf.getInt("wait"))
                            attackEndedWaiter.set(false); return@repeatScheduler
                        }
                        val c = attackEndedCount.get()
                        if (c == 0) { fireNotInAttackEvent(); return@repeatScheduler }
                        attackEndedCount.set(c - 1)
                    }
                }
            }
        }
    }

    fun setAttackMethod(method: Collection<AttackState>) {
        if (!inAttack.get()) return
        attackMethods.clear(); attackMethods.addAll(method)
        EventManager.triggerEvent(UnderAttackEvent(attackMethods))
    }

    fun fireAttackEvent() {
        if (LockdownManager.state.get()) {
            EventManager.triggerEvent(UnderAttackEvent(listOf(AttackState.LOCKDOWN)))
        } else if (attackMethods.isEmpty()) {
            EventManager.triggerEvent(UnderAttackEvent(listOf(AttackState.NOT_HANDLED)))
        } else {
            EventManager.triggerEvent(UnderAttackEvent(attackMethods))
        }
        inAttack.set(true)
        if (inAttack.get()) { duration.start() }
    }

    fun fireNotInAttackEvent() { EventManager.triggerEvent(AttackStoppedEvent()); attackMethods.clear(); inAttack.set(false); duration.stop() }

}