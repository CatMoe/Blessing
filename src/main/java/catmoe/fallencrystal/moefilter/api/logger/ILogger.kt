package catmoe.fallencrystal.moefilter.api.logger

import java.util.logging.LogRecord

interface ILogger {
    fun isLoggable(record: LogRecord?): Boolean
}