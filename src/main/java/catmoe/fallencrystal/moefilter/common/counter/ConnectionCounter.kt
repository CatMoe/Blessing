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

package catmoe.fallencrystal.moefilter.common.counter

import catmoe.fallencrystal.moefilter.MoeFilter
import catmoe.fallencrystal.moefilter.common.config.LocalConfig
import catmoe.fallencrystal.moefilter.common.counter.type.BlockType
import catmoe.fallencrystal.moefilter.common.state.AttackState
import catmoe.fallencrystal.moefilter.common.state.StateManager
import catmoe.fallencrystal.moefilter.util.plugin.util.Scheduler
import com.github.benmanes.caffeine.cache.Caffeine
import java.net.InetAddress
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

@Suppress("MemberVisibilityCanBePrivate")
object ConnectionCounter {
    var total: Long = 0
    var sessionTotal: Long = 0
    var sessionPeakCps = 0
    private var inAttack = false
    // Startup schedule to put value when after 100 milliseconds.

    init { Scheduler(MoeFilter.instance).repeatScheduler(50, TimeUnit.MILLISECONDS) { schedule() } }

    fun schedule() {
        putCPStoCache()
        if (StateManager.inAttack.get()) {
            attackEndedDetector(); attackMethodAnalyser()
        } else if (getConnectionPerSec() >= LocalConfig.getAntibot().getInt("attack-mode.incoming")) {
            StateManager.fireAttackEvent()
            inAttack = false
            sessionTotal = 0
            sessionPeakCps = 0
            sessionTotalIps = 0
            sessionBlocked.clear()
            lastMethod.clear()
        }
    }

    private val attackEndedWaiter = AtomicBoolean(false)
    private val attackEndedCount = AtomicInteger(0)

    private val lastMethod: MutableCollection<AttackState> = ArrayList()

    private fun attackMethodAnalyser() {
        if (!StateManager.inAttack.get()) return
        val conf = LocalConfig.getAntibot().getConfig("attack-mode")
        val cps = getConnectionPerSec()
        val inc = conf.getInt("incoming")
        if (cps < inc && StateManager.attackMethods.isNotEmpty()) return
        val methodSize = AttackState.values().size
        if (cps >= methodSize) {
            val method: MutableCollection<AttackState> = ArrayList()
            if (cps > methodSize * 2) {
                BlockType.values().forEach { if ((sessionBlocked[it] ?: 0) > cps / methodSize) { method.add(it.state) } }
                if (method == lastMethod) return
                lastMethod.clear(); lastMethod.addAll(method)
                StateManager.setAttackMethod(method); return
            }
        }
    }

    private fun attackEndedDetector() {
        val conf = LocalConfig.getAntibot().getConfig("attack-mode.un-attacked")
        val cps = getConnectionPerSec()
        if (inAttack && cps == 0) {
            if (conf.getBoolean("instant")) { StateManager.fireNotInAttackEvent() }
            else {
                if (!attackEndedWaiter.get()) {
                    attackEndedWaiter.set(true)
                    Scheduler(MoeFilter.instance).repeatScheduler(1, 1, TimeUnit.SECONDS) {
                        if (!attackEndedWaiter.get()) { attackEndedCount.set(conf.getInt("wait") + 1); return@repeatScheduler }
                        if (getConnectionPerSec() != 0) {
                            attackEndedCount.set(conf.getInt("wait"))
                            attackEndedWaiter.set(false); return@repeatScheduler
                        }
                        val c = attackEndedCount.get()
                        if (c == 0) { StateManager.fireNotInAttackEvent(); return@repeatScheduler }
                        attackEndedCount.set(c - 1)
                    }
                }
            }
        }
    }

    private val ticks = 1..20
    // in time (50ms)
    private var tempCPS = 0
    var peakCps = 0
    /*
    Int, Int = Ticks, Count
     */
    private val perSecCache = Caffeine.newBuilder().expireAfterWrite(1, TimeUnit.SECONDS).build<Int, Int>()
    private val ipCache = Caffeine.newBuilder().build<InetAddress, Boolean>()

    var totalIps: Long = 0
    var sessionTotalIps: Long = 0

    fun getConnectionPerSec(): Int { var cps=0; ticks.forEach { cps+=(perSecCache.getIfPresent(it) ?: 0) }; cps+= tempCPS; return cps }
    fun increase(address: InetAddress) {
        if (ipCache.getIfPresent(address) != true) { totalIps++; sessionTotalIps++; ipCache.put(address, true) }
        total++; tempCPS++; if (inAttack) { sessionTotal++; }
        /* if (getConnectionPerSec() > peakCPS) { peakCPS = getConnectionPerSec(); peakCpsInSession = if (inAttack) peakCPS else 0 } */
        val cps = getConnectionPerSec()
        if (cps > peakCps) { peakCps = cps }
        if (inAttack) { if (cps > sessionPeakCps) { sessionPeakCps = cps } }
    }
    // fun getPeakSession(): Int { return peakInSession }
    private fun putCPStoCache() { ticks.forEach { if (perSecCache.getIfPresent(it) == null) { perSecCache.put(it, tempCPS); tempCPS = 0; return } } }
    fun setInAttack(inAttacking: Boolean) { inAttack = inAttacking }


    /* Block */

    val blocked: MutableMap<BlockType, Long> = mutableMapOf()
    val sessionBlocked: MutableMap<BlockType, Long> = mutableMapOf()

    fun countBlocked(type: BlockType) {
        blocked[type] = ((blocked[type] ?: 0) +1)
        if (inAttack) { sessionBlocked[type] = ((sessionBlocked[type] ?: 0)+1) }
    }

    fun totalBlocked(): Long {
        var count: Long = 0
        BlockType.values().forEach { count += blocked[it] ?: 0 }
        return count
    }

    fun totalSessionBlocked(): Long {
        var count: Long = 0
        BlockType.values().forEach { count += sessionBlocked[it] ?: 0 }
        return count
    }

}