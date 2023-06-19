package catmoe.fallencrystal.moefilter.network.bungee.pipeline

interface IPipeline {
    companion object {
        const val HANDLER = "moefilter-handler"
        const val DECODER = "moefilter-decoder"
        const val PACKET_INTERCEPTOR = "moefilter-packet-interceptor"
        const val LAST_PACKET_INTERCEPTOR = "moefilter-packet-exception-interceptor"
    }
}
