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
    var totalInSession: Long = 0
    var peakCpsInSession: Int = 0
    var peakIpSecInSession: Int = 0
    private var inAttack = false
    // Startup schedule to put value when after 100 milliseconds.
    init { Scheduler(MoeFilter.instance).repeatScheduler(50, TimeUnit.MILLISECONDS) { schedule() } }

    fun schedule() {
        putCPStoCache(); putIpSecToCache()
        if (StateManager.inAttack.get()) {
            if (inAttack) { attackEndedDetector(); attackMethodAnalyser()
            } else {
                inAttack = false; totalInSession = 0; peakCpsInSession = 0; peakIpSecInSession = 0
                sessionBlocked.clear()
            }
        } else if (getConnectionPerSec() >= LocalConfig.getAntibot().getInt("attack-mode.incoming"))
        { StateManager.fireAttackEvent() }
    }

    private val attackEndedWaiter = AtomicBoolean(false)
    private val attackEndedCount = AtomicInteger(0)

    private fun attackMethodAnalyser() {
        if (!StateManager.inAttack.get()) return
        val conf = LocalConfig.getAntibot().getConfig("attack-mode")
        val cps = getConnectionPerSec()
        val inc = conf.getInt("incoming")
        if (cps < inc && StateManager.attackMethods.isNotEmpty()) {
            StateManager.setAttackMethod(listOf()); return
        }
        val methodSize = AttackState.values().size
        if (cps >= methodSize) {
            val method: MutableCollection<AttackState> = ArrayList()
            if (cps > methodSize * 2) {
                BlockType.values().forEach { if ((sessionBlocked[it] ?: 0) > cps / methodSize) { method.add(it.state) } }
                StateManager.setAttackMethod(method); return
            }
        }
    }

    private fun attackEndedDetector() {
        val conf = LocalConfig.getAntibot().getConfig("attack-mode")
        val cps = getConnectionPerSec()
        if (inAttack && cps == 0) {
            if (conf.getBoolean("un-attacked.instant")) { StateManager.fireNotInAttackEvent() }
            else {
                if (!attackEndedWaiter.get()) {
                    attackEndedWaiter.set(true)
                    Scheduler(MoeFilter.instance).repeatScheduler(1, 1, TimeUnit.SECONDS) {
                        if (!attackEndedWaiter.get()) { attackEndedCount.set(conf.getInt("un-attacked.wait")); return@repeatScheduler }
                        if (getConnectionPerSec() != 0) {
                            attackEndedCount.set(conf.getInt("un-attacked.wait"))
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
    private var tempIpSec = 0
    var peakCps = 0
    var peakIpSec = 0
    /*
    Int, Int = Ticks, Count
     */
    private val perSecCache = Caffeine.newBuilder().expireAfterWrite(1, TimeUnit.SECONDS).build<Int, Int>()
    private val ipCache = Caffeine.newBuilder().expireAfterWrite(1, TimeUnit.SECONDS).build<InetAddress, Int>()
    private val ipPerSecCache =  Caffeine.newBuilder().expireAfterWrite(1, TimeUnit.SECONDS).build<Int, Int>()
    fun getConnectionPerSec(): Int { var cps=0; ticks.forEach { cps+=(perSecCache.getIfPresent(it) ?: 0) }; cps+= tempCPS; return cps }
    fun getIpPerSec(): Int { var ipPerSec=0; ticks.forEach { ipPerSec+=(ipPerSecCache.getIfPresent(it) ?: 0) }; ipPerSec+= tempIpSec; return ipPerSec }
    fun increase(address: InetAddress) {
        val singleIpCount = ipCache.getIfPresent(address) ?: 0
        if (singleIpCount == 0) { tempIpSec++; ipCache.put(address, 1) }
        total++; tempCPS++; if (inAttack) { totalInSession++; } else { totalInSession = 0 }
        /* if (getConnectionPerSec() > peakCPS) { peakCPS = getConnectionPerSec(); peakCpsInSession = if (inAttack) peakCPS else 0 } */
        val cps = getConnectionPerSec()
        val ipSec = getIpPerSec()
        if (cps > peakCps) { peakCps = cps }; if (ipSec > peakIpSec) { peakIpSec = ipSec }
        if (inAttack) { if (cps > peakCpsInSession) { peakCpsInSession = cps }; if (ipSec > peakIpSecInSession) { peakCpsInSession = ipSec } }
    }
    // fun getPeakSession(): Int { return peakInSession }
    private fun putCPStoCache() { ticks.forEach { if (perSecCache.getIfPresent(it) == null) { perSecCache.put(it, tempCPS); tempCPS = 0; return } } }
    private fun putIpSecToCache() { ticks.forEach { if (ipPerSecCache.getIfPresent(it) == null) { ipPerSecCache.put(it, tempIpSec); tempIpSec = 0; return } } }
    fun setInAttack(inAttacking: Boolean) { inAttack = inAttacking }


    /* Block */

    val blocked: MutableMap<BlockType, Long> = mutableMapOf()
    val sessionBlocked: MutableMap<BlockType, Long> = mutableMapOf()

    fun countBlocked(type: BlockType) {
        blocked[type] = ((blocked[type] ?: 0) +1);
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