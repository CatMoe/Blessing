package catmoe.fallencrystal.moefilter.common.whitelist

import com.github.benmanes.caffeine.cache.Caffeine
import java.net.InetAddress

object WhitelistObject {
    private val cache = Caffeine.newBuilder().build<InetAddress, Boolean>()
    private val ips = mutableListOf<InetAddress>()

    fun getWhitelist(address: InetAddress) { cache.getIfPresent(address) ?: false }

    fun setWhitelist(address: InetAddress, type: WhitelistType) {
        when (type) {
            WhitelistType.ADD -> { addWhitelist(address) }
            WhitelistType.REMOVE -> { removeWhitelist(address) }
        }
    }

    fun getAllWhitelist(): List<InetAddress> { return ips }

    private fun addWhitelist(address: InetAddress) {
        if (cache.getIfPresent(address) != null && ips.contains(address)) return
        cache.put(address, true)
        ips.add(address)
    }

    private fun removeWhitelist(address: InetAddress) {
        if (cache.getIfPresent(address) == null && !ips.contains(address)) return
        cache.invalidate(address)
        ips.remove(address)
    }
}