package catmoe.fallencrystal.moefilter.api.logger

import java.util.logging.Filter
import java.util.logging.LogRecord

object LoggerManager : Filter {
    private val logger: MutableList<ILogger>
    private val filter: MutableList<String>
    init {
        logger = ArrayList()
        filter = ArrayList()
    }

    override fun isLoggable(record: LogRecord?): Boolean {
        var loggable = true
        // 检测是否为空 如果空则忽略 否则forEach检查是否包含消息
        if (filter.isNotEmpty()) filter.forEach { if (record?.message?.contains(it) == true) return false }
        // 如果ILogger为空时 默认true
        if (logger.isEmpty()) return true
        logger.forEach { if (!it.isLoggable(record)) loggable = false }
        return loggable
    }

    fun registerLogger(c: ILogger) { logger.add(c) }

    fun unregisterLogger(c: ILogger) {
        if (!logger.contains(c)) throw NullPointerException("$c is not registered logger!")
        logger.remove(c)
    }

    fun getFilterList(): MutableList<String> {return filter}

    fun registerFilter(text: String) { if (filter.contains(text)) return else filter.add(text) }

    fun unregisterFilter(text: String) {
        if (!filter.contains(text)) throw NullPointerException("$text is not found in filter list!")
        filter.remove(text)
    }
}