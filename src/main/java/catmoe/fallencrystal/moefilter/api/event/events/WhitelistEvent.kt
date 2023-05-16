package catmoe.fallencrystal.moefilter.api.event.events

import catmoe.fallencrystal.moefilter.common.whitelist.WhitelistType

class WhitelistEvent(val address: String, val type: WhitelistType)