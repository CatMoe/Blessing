package catmoe.fallencrystal.moefilter.util.message.kick

import catmoe.fallencrystal.moefilter.common.config.ObjectConfig

enum class KickType(@JvmField val originalMessage: List<String>) {
    BLACKLISTED(ObjectConfig.getMessage().getStringList("kick.blacklisted")),
    REJOIN(ObjectConfig.getMessage().getStringList("kick.rejoin")),
    PING_FIRST(ObjectConfig.getMessage().getStringList("kick.ping"))
}