package catmoe.fallencrystal.moefilter.network.bungee.util.kick

enum class DisconnectType(@JvmField val messagePath: String) {
    ALREADY_ONLINE("kick.already-online"),
    REJOIN("kick.rejoin"),
    PING("kick.ping"),
    INVALID_NAME("kick.invalid-name")
}