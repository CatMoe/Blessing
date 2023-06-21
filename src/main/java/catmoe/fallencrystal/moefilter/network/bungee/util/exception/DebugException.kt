package catmoe.fallencrystal.moefilter.network.bungee.util.exception

class DebugException(override val message: String) : RuntimeException() {
    override fun initCause(cause: Throwable): Throwable { return this }
    override fun fillInStackTrace(): Throwable { return this }
}