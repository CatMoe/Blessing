package catmoe.fallencrystal.moefilter.common.blacklist

import catmoe.fallencrystal.moefilter.api.event.EventListener
import catmoe.fallencrystal.moefilter.api.event.FilterEvent
import catmoe.fallencrystal.moefilter.api.event.events.BlacklistEvent

class BlacklistListener : EventListener {
    @FilterEvent
    fun onBlacklist(event: BlacklistEvent) {
        val address = event.profile.ip
        BlacklistObject.setBlacklist(address, event.profile)
    }
}