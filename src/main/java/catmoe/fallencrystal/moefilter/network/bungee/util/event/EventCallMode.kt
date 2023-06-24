package catmoe.fallencrystal.moefilter.network.bungee.util.event

enum class EventCallMode {
    AFTER_INIT, // This will call event when connection incoming. Whether they are blocked by throttle or by firewall or not.
    NON_FIREWALL, // When the connection is not blocked by a firewall. It will call event. (priority: firewall > throttle)
    READY_DECODING, // Call event before decoder. If is canceled. We will close the pipeline. (Throttle may close the connection first.)
    AFTER_DECODER, // Call the event after the base pipeline process and decoder. ( not recommend )
    DISABLED // Call the void :D. to save performance.
}