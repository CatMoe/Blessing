package catmoe.fallencrystal.moefilter.common.whitelist

import catmoe.fallencrystal.moefilter.api.event.EventListener
import catmoe.fallencrystal.moefilter.api.event.FilterEvent
import catmoe.fallencrystal.moefilter.api.event.events.WhitelistEvent

class WhitelistListener : EventListener {
    @FilterEvent
    fun onChangeWhitelist(event: WhitelistEvent) { WhitelistObject.setWhitelist(event.address, event.type) }
}