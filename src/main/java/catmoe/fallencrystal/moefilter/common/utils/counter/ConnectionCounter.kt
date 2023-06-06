package catmoe.fallencrystal.moefilter.common.utils.counter

import catmoe.fallencrystal.moefilter.util.plugin.FilterPlugin
import com.github.benmanes.caffeine.cache.Caffeine
import net.md_5.bungee.api.ProxyServer
import java.net.InetAddress
import java.util.concurrent.TimeUnit

object ConnectionCounter {
    private var total: Long = 0
    private var totalInSession: Long = 0
    private var inAttack = false
    // Startup schedule to put value when after 100 milliseconds.
    init { ProxyServer.getInstance().scheduler.schedule(FilterPlugin.getPlugin(), { putCPStoCache(); putIpSecToCache(); },0, 100, TimeUnit.MILLISECONDS) }
    private val ticks: List<Int> = listOf(1, 2, 3, 4, 5, 6, 7, 8 ,9 ,10)
    // in time (100ms)
    private var tempCPS = 0
    private var tempIpSec = 0
    /*
    Int, Int = Time(1t = 2ticks = 100ms), Count
     */
    private val connectionPerSecCache = Caffeine.newBuilder().expireAfterWrite(1, TimeUnit.SECONDS).build<Int, Int>()
    private val ipCache = Caffeine.newBuilder().expireAfterWrite(1, TimeUnit.SECONDS).build<InetAddress, Int>()
    private val ipPerSecCache = Caffeine.newBuilder().expireAfterWrite(1, TimeUnit.SECONDS).build<Int, Int>()

    fun getTotal(): Long { return total }
    fun getTotalSession(): Long { return totalInSession }
    fun getConnectionPerSec(): Int { var cps=0; ticks.forEach { cps+=(connectionPerSecCache.getIfPresent(it) ?: 0) }; cps+= tempCPS; return cps }
    fun getIpPerSec(): Int { var ipPerSec=0; ticks.forEach { ipPerSec+=(ipPerSecCache.getIfPresent(it) ?: 0) }; ipPerSec+= tempIpSec; return ipPerSec }
    fun increase(address: InetAddress) {
        val singleIpCount = ipCache.getIfPresent(address) ?: 0
        if (singleIpCount == 0) { tempIpSec++; ipCache.put(address ,1) } else ipCache.put(address, singleIpCount + 1)
        total++; tempCPS++; if (inAttack) { totalInSession++ } else { totalInSession = 0 }
    }
    private fun putCPStoCache() { ticks.forEach { if (connectionPerSecCache.getIfPresent(it) == null) { connectionPerSecCache.put(it, tempCPS); tempCPS = 0; return } } }
    private fun putIpSecToCache() { ticks.forEach { if (ipPerSecCache.getIfPresent(it) == null) { ipPerSecCache.put(it, tempIpSec); tempIpSec = 0; return } } }
    fun setInAttack(inAttacking: Boolean) { inAttack =inAttacking }

}