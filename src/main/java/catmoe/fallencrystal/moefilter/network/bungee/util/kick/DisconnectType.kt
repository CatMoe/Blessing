package catmoe.fallencrystal.moefilter.network.bungee.util.kick

enum class DisconnectType(@JvmField val messagePath: String) {
    ALREADY_ONLINE("kick.already-online"),
    FIRST_JOIN("kick.rejoin")
}