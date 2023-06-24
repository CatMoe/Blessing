package catmoe.fallencrystal.moefilter.listener.main

import catmoe.fallencrystal.moefilter.api.logger.ILogger
import java.util.logging.LogRecord

class ExceptionFilter : ILogger {

    private val filterException = listOf(
        "QuietException",
        "FastException",
        "BadPacketException",
        "FastOverflowPacketException",
    )

    override fun isLoggable(record: LogRecord?, isCancelled: Boolean): Boolean {
        if (isCancelled || record == null) return false
        return if (record.thrown?.cause != null) { !filterException.contains(record.thrown.cause!!::class.java.name) } else record.message?.contains("InitialHandler - encountered exception") != true
    }
}