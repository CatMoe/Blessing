package catmoe.fallencrystal.moefilter.common.whitelist

import catmoe.fallencrystal.moefilter.api.event.EventManager
import catmoe.fallencrystal.moefilter.api.event.events.WhitelistEvent
import com.github.benmanes.caffeine.cache.Caffeine

object WhitelistObject {
    private val cache = Caffeine.newBuilder().build<String, Boolean>()
    private val ips = mutableListOf<String>()

    fun getWhitelist(address: String) { cache.getIfPresent(address) ?: false }

    fun setWhitelist(address: String, type: WhitelistType) {
        when (type) {
            WhitelistType.ADD -> { addWhitelist(address) }
            WhitelistType.REMOVE -> { removeWhitelist(address) }
        }
    }

    fun getAllWhitelist(): List<String> { return ips }

    private fun addWhitelist(address: String) {
        if (cache.getIfPresent(address) != null && ips.contains(address)) return
        cache.put(address, true)
        ips.add(address)
    }

    private fun removeWhitelist(address: String) {
        if (cache.getIfPresent(address) == null && !ips.contains(address)) return
        cache.invalidate(address)
        ips.remove(address)
    }

    private fun triggerEvent(address: String, type: WhitelistType) { EventManager.triggerEvent(WhitelistEvent(address, type)) }
}