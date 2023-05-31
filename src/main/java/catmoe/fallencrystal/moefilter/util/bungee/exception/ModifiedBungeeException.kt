package catmoe.fallencrystal.moefilter.util.bungee.exception

class ModifiedBungeeException(message: String?) : RuntimeException(message) {
    override fun initCause(cause: Throwable): Throwable { return this }

    override fun fillInStackTrace(): Throwable { return this }
}
