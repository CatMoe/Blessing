package catmoe.fallencrystal.moefilter.network.bungee.util

class InvalidStatusPingException : RuntimeException() {
    override fun initCause(cause: Throwable): Throwable { return this }

    override fun fillInStackTrace(): Throwable { return this }
}