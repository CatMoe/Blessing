package catmoe.fallencrystal.moefilter.common.attack

import catmoe.fallencrystal.moefilter.common.config.ObjectConfig

enum class AttackType(@JvmField val typeName: String) {
    /*
    Join Methods
     */
    JOIN(ObjectConfig.getMessage().getString("methods.JOIN")),

    ONCE_JOIN(ObjectConfig.getMessage().getString("methods.ONCE-JOIN")),
    PING_AND_JOIN(ObjectConfig.getMessage().getString("methods.PING-AND-JOIN")),
    REJOIN(ObjectConfig.getMessage().getString("methods.REJOIN")),
    LONGER_NAME(ObjectConfig.getMessage().getString("methods.LONGER-NAME")),
    BAD_NAME(ObjectConfig.getMessage().getString("methods.BAD-NAME")),


    /*
    Ping Methods
     */
    PING(ObjectConfig.getMessage().getString("methods.PING")),

    EXCEPTION_PING(ObjectConfig.getMessage().getString("methods.EXCEPTION-PING")),
    PING_FLOOD(ObjectConfig.getMessage().getString("methods.PING-FLOOD")),

    /*
    General Methods
     */
    UNKNOWN_PROTOCOL(ObjectConfig.getMessage().getString("methods.UNKNOWN-PROTOCOL")),
    MALFORMED_PACKET(ObjectConfig.getMessage().getString("methods.MALFORMED_PACKET")),
}