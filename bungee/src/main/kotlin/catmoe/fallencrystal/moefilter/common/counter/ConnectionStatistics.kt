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

package catmoe.fallencrystal.moefilter.common.counter

import catmoe.fallencrystal.moefilter.common.state.StateManager
import catmoe.fallencrystal.moefilter.data.BlockType
import catmoe.fallencrystal.moefilter.util.plugin.util.Scheduler
import catmoe.fallencrystal.translation.utils.config.LocalConfig
import com.github.benmanes.caffeine.cache.Caffeine.newBuilder
import java.net.InetAddress
import java.util.concurrent.TimeUnit

@Suppress("MemberVisibilityCanBePrivate", "EnumValuesSoftDeprecate")
object ConnectionStatistics {
    var total: Long = 0
    var sessionTotal: Long = 0
    var sessionPeakCps = 0
    var inAttack = false
    // Startup schedule to put value when after 100 milliseconds.

    init { Scheduler.getDefault().repeatScheduler(50, TimeUnit.MILLISECONDS) { schedule() } }

    fun schedule() {
        putCPStoCache()
        putBytesToCache()
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
        StateManager.tickProfile()
    }

    private val ticks = 1..20
    // in time (50ms)
    private var tempCPS = 0
    var peakCps = 0
    /*
    Int, Int = Ticks, Count
     */
    private val perSecCache = newBuilder().expireAfterWrite(1, TimeUnit.SECONDS).build<Int, Int>()
    private val ipCache = newBuilder().build<InetAddress, Boolean>()
    val sessionIpCache = newBuilder().build<InetAddress, Boolean>()

    var totalIps: Long = 0
    var sessionTotalIps: Long = 0

    fun getConnectionPerSec(): Int { var cps=0; ticks.forEach { cps+=(perSecCache.getIfPresent(it) ?: 0) }; cps+= tempCPS; return cps }
    fun increase(address: InetAddress) {
        if (ipCache.getIfPresent(address) != true) { totalIps++; ipCache.put(address, true) }
        total++; tempCPS++; if (inAttack) { sessionTotal++; }
        val cps = getConnectionPerSec()
        if (cps > peakCps) { peakCps = cps }
        if (inAttack) { if (cps > sessionPeakCps) { sessionPeakCps = cps }; if (sessionIpCache.getIfPresent(address) != true) { sessionIpCache.put(address, true); sessionTotalIps++ } }
    }
    private fun putCPStoCache() { ticks.forEach { if (perSecCache.getIfPresent(it) == null) { perSecCache.put(it, tempCPS); tempCPS = 0; return } } }


    /* Block */

    val blocked = newBuilder().build<BlockType, Long>()
    val sessionBlocked = newBuilder().build<BlockType, Long>()

    fun countBlocked(type: BlockType) {
        blocked.put(type, (blocked.getIfPresent(type) ?: 0) + 1)
        if (inAttack) sessionBlocked.put(type, (sessionBlocked.getIfPresent(type) ?: 0) + 1)
    }

    fun totalBlocked(): Long {
        var c: Long = 0; BlockType.values().forEach { c += blocked.getIfPresent(it) ?: 0  }; return c
    }

    /* Bytes */

    val incoming = newBuilder().expireAfterWrite(1, TimeUnit.SECONDS).build<Int, Long>()
    val outgoing = newBuilder().expireAfterWrite(1, TimeUnit.SECONDS).build<Int, Long>()

    var incomingBytes: Long = 0
    var outgoingBytes: Long = 0

    fun increaseIncoming(size: Int) { incomingBytes+=size }

    fun increaseOutgoing(size: Int) { outgoingBytes+=size }

    fun putBytesToCache() {
        ticks.forEach {
            if (incoming.getIfPresent(it) == null || outgoing.getIfPresent(it) == null) {
                incoming.put(it, incomingBytes); incomingBytes=0
                outgoing.put(it, outgoingBytes); outgoingBytes=0
                return
            }
        }
    }

    fun getIncoming(): Long {
        var result: Long = 0
        ticks.forEach { result += (incoming.getIfPresent(it) ?: 0) }
        result += incomingBytes
        return result
    }

    fun getOutgoing(): Long {
        var result: Long = 0
        ticks.forEach { result += (outgoing.getIfPresent(it) ?: 0) }
        result += outgoingBytes
        return result
    }

    fun totalSessionBlocked(): Long {
        var c: Long = 0; BlockType.values().forEach { c += sessionBlocked.getIfPresent(it) ?: 0 }; return c
    }

}