package catmoe.fallencrystal.moefilter.api.command.impl.log

import catmoe.fallencrystal.moefilter.api.logger.ILogger
import java.util.logging.LogRecord

class LogHandler : ILogger {
    override fun isLoggable(record: LogRecord?): Boolean { record?.let { LogBroadcast.broadcast(it) }; return true }
}