package catmoe.fallencrystal.moefilter.common.check.ping_and_join

enum class CheckType {
    PING_AFTER_RECONNECT,
    RECONNECT_AFTER_PING,
    ONLY_PING,
    ONLY_RECONNECT,
    PING_RECONNECT_STABLE,
}