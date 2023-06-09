package catmoe.fallencrystal.moefilter.common.utils.maxmind.exception

class InvalidMaxmindKeyException(message: String?) : RuntimeException(message){
    override fun initCause(cause: Throwable?): Throwable { return this }
    override fun fillInStackTrace(): Throwable { return this }
}