package catmoe.fallencrystal.moefilter.common.check.rejoin

import com.github.benmanes.caffeine.cache.Caffeine
import java.net.InetAddress
import java.util.concurrent.TimeUnit

object RejoinManager {
    private val joinCache = Caffeine.newBuilder().expireAfterWrite(30, TimeUnit.SECONDS).build<InetAddress, String>()

    // Username, Address: Passed
    fun increase(username: String, inetAddress: InetAddress): Boolean {
        if (joinCache.getIfPresent(inetAddress)?.equals(username) == true) { return true }
        else { joinCache.put(inetAddress, username); return false }
    }
}