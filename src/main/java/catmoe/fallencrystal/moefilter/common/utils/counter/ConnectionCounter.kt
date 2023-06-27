package catmoe.fallencrystal.moefilter.common.utils.counter

import catmoe.fallencrystal.moefilter.MoeFilter
import catmoe.fallencrystal.moefilter.util.plugin.util.Scheduler
import com.github.benmanes.caffeine.cache.Caffeine
import com.google.common.cache.CacheBuilder
import java.net.InetAddress
import java.util.concurrent.TimeUnit

object ConnectionCounter {
    private var total: Long = 0
    private var totalInSession: Long = 0
    private var peakInSession: Int = 0
    private var inAttack = false
    // Startup schedule to put value when after 100 milliseconds.
    init { Scheduler(MoeFilter.instance).repeatScheduler(50, TimeUnit.MILLISECONDS) { putCPStoCache(); putIpSecToCache() } }
    private val ticks: List<Int> = listOf(1, 2, 3, 4, 5, 6, 7, 8 ,9 ,10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20)
    // in time (50ms)
    private var tempCPS = 0
    private var tempIpSec = 0
    private var peakCPS = 0
    /*
    Int, Int = Ticks, Count
     */
    private val connectionPerSecCache = Caffeine.newBuilder().expireAfterWrite(1, TimeUnit.SECONDS).build<Int, Int>()
    private val ipCache = Caffeine.newBuilder().expireAfterWrite(1, TimeUnit.SECONDS).build<InetAddress, Int>()
    private val ipPerSecCache = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.SECONDS).build<Int, Int>()
    fun getConnectionPerSec(): Int { var cps=0; ticks.forEach { cps+=(connectionPerSecCache.getIfPresent(it) ?: 0) }; cps+=tempCPS; return cps }
    fun getIpPerSec(): Int { var ipPerSec=0; ticks.forEach { ipPerSec+=(ipPerSecCache.getIfPresent(it) ?: 0) }; ipPerSec+=tempIpSec; return ipPerSec }
    fun getPeakConnectionPerSec(): Int { return peakCPS }
    fun increase(address: InetAddress) {
        val singleIpCount = ipCache.getIfPresent(address) ?: 0
        if (singleIpCount == 0) { tempIpSec++; ipCache.put(address, 1) }
        total++; tempCPS++; if (inAttack) { totalInSession++; } else { totalInSession = 0 }
        if (getConnectionPerSec() > peakCPS) { peakCPS=getConnectionPerSec(); peakInSession = if (inAttack) peakCPS else 0 }
    }
    fun getTotal(): Long { return total }
    fun getTotalSession(): Long { return totalInSession }
    // fun getPeakSession(): Int { return peakInSession }
    private fun putCPStoCache() { ticks.forEach { if (connectionPerSecCache.getIfPresent(it) == null) { connectionPerSecCache.put(it, tempCPS); tempCPS = 0; return } } }
    private fun putIpSecToCache() { ticks.forEach { if (ipPerSecCache.getIfPresent(it) == null) { ipPerSecCache.put(it, tempIpSec); tempIpSec = 0; return } } }
    fun setInAttack(inAttacking: Boolean) { inAttack =inAttacking }

}