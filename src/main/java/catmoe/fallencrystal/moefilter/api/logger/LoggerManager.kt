package catmoe.fallencrystal.moefilter.api.logger

import java.util.logging.Filter
import java.util.logging.LogRecord

object LoggerManager : Filter {
    private val logger: MutableList<ILogger>
    init { logger = ArrayList() }

    override fun isLoggable(record: LogRecord?): Boolean {
        var loggable = true
        // 如果ILogger为空时 默认true
        if (logger.isEmpty()) return true
        logger.forEach { if (!it.isLoggable(record)) loggable = false }
        return loggable
    }

    fun registerFilter(c: ILogger) { logger.add(c) }

    fun unregisterFilter(c: ILogger) {
        if (!logger.contains(c)) throw NullPointerException("$c is not registered logger!")
        logger.remove(c)
    }

}