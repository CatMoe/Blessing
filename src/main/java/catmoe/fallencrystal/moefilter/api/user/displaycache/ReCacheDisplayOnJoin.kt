package catmoe.fallencrystal.moefilter.api.user.displaycache

import catmoe.fallencrystal.moefilter.api.event.EventListener
import catmoe.fallencrystal.moefilter.api.event.FilterEvent
import catmoe.fallencrystal.moefilter.api.event.events.bungee.AsyncPostLoginEvent

class ReCacheDisplayOnJoin : EventListener {
    @FilterEvent
    fun onUpdateDisplayOnJoin(event: AsyncPostLoginEvent) { DisplayCache.getDisplay(event.player.uniqueId) }
}