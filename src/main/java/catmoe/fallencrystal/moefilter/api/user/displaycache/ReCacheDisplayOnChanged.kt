package catmoe.fallencrystal.moefilter.api.user.displaycache

import catmoe.fallencrystal.moefilter.api.event.EventListener
import catmoe.fallencrystal.moefilter.api.event.FilterEvent
import catmoe.fallencrystal.moefilter.api.event.events.luckperms.PermsUserDataRecalculateEvent

class ReCacheDisplayOnChanged : EventListener {
    @FilterEvent
    fun onUpdated(event: PermsUserDataRecalculateEvent) {
        val uuid = event.user.uniqueId
        val metaData = event.data.metaData
        val prefix = metaData.prefix
        val suffix = metaData.suffix
        DisplayCache.updateDisplayCache(uuid, Display(prefix ?: "", suffix ?: ""))
    }
}