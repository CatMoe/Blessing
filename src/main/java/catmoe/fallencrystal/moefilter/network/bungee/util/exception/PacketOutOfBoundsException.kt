package catmoe.fallencrystal.moefilter.network.bungee.util.exception

class PacketOutOfBoundsException(override val message: String) : RuntimeException() {
    override fun initCause(cause: Throwable): Throwable { return this }
    override fun fillInStackTrace(): Throwable { return this }
}
