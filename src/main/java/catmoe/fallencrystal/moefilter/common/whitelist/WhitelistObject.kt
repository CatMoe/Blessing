package catmoe.fallencrystal.moefilter.common.whitelist

import com.github.benmanes.caffeine.cache.Caffeine

object WhitelistObject {
    private val cache = Caffeine.newBuilder().build<String, Boolean>()
    private val ips = mutableListOf<String>()

    fun getWhitelist(address: String) { cache.getIfPresent(address) ?: false }

    fun addWhitelist(address: String) {
        val whitelisted = cache.getIfPresent(address)
        if (whitelisted != null) return
        cache.put(address, true)
        ips.add(address)
    }

    fun removeWhitelist(address: String) {
        val whitelisted = cache.getIfPresent(address)
        if (whitelisted != null) {
            cache.invalidate(address)
            ips.remove(address)
        }
    }

    fun getAllWhitelist(): List<String> { return ips }
}