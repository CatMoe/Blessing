package catmoe.fallencrystal.moefilter.network.bungee.handler

enum class ConnectionState {
    HANDSHAKE,
    STATUS,
    PINGING,
    JOINING,
    PROCESSING
}