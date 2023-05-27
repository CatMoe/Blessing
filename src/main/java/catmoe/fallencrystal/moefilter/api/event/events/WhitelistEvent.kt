package catmoe.fallencrystal.moefilter.api.event.events

import catmoe.fallencrystal.moefilter.common.whitelist.WhitelistType
import java.net.InetAddress

class WhitelistEvent(val address: InetAddress, val type: WhitelistType)