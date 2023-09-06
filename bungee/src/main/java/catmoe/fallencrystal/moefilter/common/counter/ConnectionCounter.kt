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

import catmoe.fallencrystal.moefilter.MoeFilterBungee
import catmoe.fallencrystal.moefilter.common.counter.type.BlockType
import catmoe.fallencrystal.moefilter.common.state.StateManager
import catmoe.fallencrystal.moefilter.util.plugin.util.Scheduler
import catmoe.fallencrystal.translation.utils.config.LocalConfig
import com.github.benmanes.caffeine.cache.Caffeine
import java.net.InetAddress
import java.util.concurrent.TimeUnit

@Suppress("MemberVisibilityCanBePrivate", "EnumValuesSoftDeprecate")
object ConnectionCounter {
    var total: Long = 0
    var sessionTotal: Long = 0
    var sessionPeakCps = 0
    private var inAttack = false
    // Startup schedule to put value when after 100 milliseconds.

    init { Scheduler(MoeFilterBungee.instance).repeatScheduler(50, TimeUnit.MILLISECONDS) { schedule() } }

    fun schedule() {
        putCPStoCache()
        if (StateManager.inAttack.get()) {
            StateManager.attackMethodAnalyser(); StateManager.attackEndedDetector()
        } else if (getConnectionPerSec() >= LocalConfig.getAntibot().getInt("attack-mode.incoming")) {
            StateManager.fireAttackEvent()
            inAttack = false
            sessionTotal = 0
            sessionPeakCps = 0
            sessionTotalIps = 0
            sessionBlocked.invalidateAll()
            StateManager.lastMethod.clear()
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
    val sessionIpCache = Caffeine.newBuilder().build<InetAddress, Boolean>()

    var totalIps: Long = 0
    var sessionTotalIps: Long = 0

    fun getConnectionPerSec(): Int { var cps=0; ticks.forEach { cps+=(perSecCache.getIfPresent(it) ?: 0) }; cps+= tempCPS; return cps }
    fun increase(address: InetAddress) {
        if (ipCache.getIfPresent(address) != true) { totalIps++; ipCache.put(address, true) }
        total++; tempCPS++; if (inAttack) { sessionTotal++; }
        /* if (getConnectionPerSec() > peakCPS) { peakCPS = getConnectionPerSec(); peakCpsInSession = if (inAttack) peakCPS else 0 } */
        val cps = getConnectionPerSec()
        if (cps > peakCps) { peakCps = cps }
        if (inAttack) { if (cps > sessionPeakCps) { sessionPeakCps = cps }; if (sessionIpCache.getIfPresent(address) != true) { sessionIpCache.put(address, true); sessionTotalIps++ } }
    }
    // fun getPeakSession(): Int { return peakInSession }
    private fun putCPStoCache() { ticks.forEach { if (perSecCache.getIfPresent(it) == null) { perSecCache.put(it, tempCPS); tempCPS = 0; return } } }
    fun setInAttack(inAttacking: Boolean) { inAttack = inAttacking }


    /* Block */

    val blocked = Caffeine.newBuilder().build<BlockType, Long>()
    val sessionBlocked = Caffeine.newBuilder().build<BlockType, Long>()

    fun countBlocked(type: BlockType) {
        blocked.put(type, (blocked.getIfPresent(type) ?: 0) + 1)
        if (inAttack) sessionBlocked.put(type, (sessionBlocked.getIfPresent(type) ?: 0) + 1)
    }

    fun totalBlocked(): Long {
        var c: Long = 0; BlockType.values().forEach { c += blocked.getIfPresent(it) ?: 0  }; return c
    }

    fun totalSessionBlocked(): Long {
        var c: Long = 0; BlockType.values().forEach { c += sessionBlocked.getIfPresent(it) ?: 0 }; return c
    }

}