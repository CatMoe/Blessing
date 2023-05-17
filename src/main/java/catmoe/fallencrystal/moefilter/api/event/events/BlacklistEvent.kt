package catmoe.fallencrystal.moefilter.api.event.events

import catmoe.fallencrystal.moefilter.common.blacklist.BlacklistProfile
import catmoe.fallencrystal.moefilter.common.blacklist.BlacklistReason

class BlacklistEvent(val profile: BlacklistProfile, val reason: BlacklistReason)