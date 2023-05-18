package catmoe.fallencrystal.moefilter.api.proxy

import com.github.benmanes.caffeine.cache.Caffeine

object ProxyCache {
    private val cache = Caffeine.newBuilder().build<String, Boolean>()

    fun fetchProxy() {val fetchProxy = FetchProxy(); fetchProxy.get()}

    fun isProxy(address: String): Boolean { return cache.getIfPresent(address) ?: false }

    fun addProxy(address: String) { cache.put(address, true) }
}