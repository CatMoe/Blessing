package catmoe.fallencrystal.moefilter.common.check.reason

import catmoe.fallencrystal.moefilter.api.event.EventListener
import catmoe.fallencrystal.moefilter.api.event.FilterEvent
import catmoe.fallencrystal.moefilter.api.event.events.PluginReloadEvent

class LoadReason : EventListener {
    @FilterEvent
    fun loadCachedReason(event: PluginReloadEvent) { CachedReason.cacheReason() }
}