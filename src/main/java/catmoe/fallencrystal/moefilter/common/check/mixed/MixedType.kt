package catmoe.fallencrystal.moefilter.common.check.mixed

enum class MixedType {
    RECONNECT,
    JOIN_AFTER_PING, // Not recommend -- Some bot can easily bypass that.
    RECONNECT_AFTER_PING,
    PING_AFTER_RECONNECT,
    STABLE,
    DISABLED
}