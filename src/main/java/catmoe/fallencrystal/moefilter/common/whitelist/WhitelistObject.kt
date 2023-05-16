package catmoe.fallencrystal.moefilter.common.whitelist

import catmoe.fallencrystal.moefilter.api.event.EventManager
import catmoe.fallencrystal.moefilter.api.event.events.WhitelistEvent
import com.github.benmanes.caffeine.cache.Caffeine

object WhitelistObject {
    private val cache = Caffeine.newBuilder().build<String, Boolean>()
    private val ips = mutableListOf<String>()

    fun getWhitelist(address: String) { cache.getIfPresent(address) ?: false }

    fun setWhitelist(address: String, type: WhitelistType) {
        if (type == WhitelistType.ADD && cache.getIfPresent(address) == null) { cache.put(address, true); triggerEvent(address, type) }
        if (type == WhitelistType.REMOVE && cache.getIfPresent(address) != null) { cache.invalidate(address); triggerEvent(address, type) }
    }

    fun getAllWhitelist(): List<String> { return ips }

    private fun triggerEvent(address: String, type: WhitelistType) { EventManager.triggerEvent(WhitelistEvent(address, type)) }
}