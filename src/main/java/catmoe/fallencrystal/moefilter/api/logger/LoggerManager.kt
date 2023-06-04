package catmoe.fallencrystal.moefilter.api.logger

import catmoe.fallencrystal.moefilter.util.message.MessageUtil
import catmoe.fallencrystal.moefilter.util.plugin.FilterPlugin
import net.md_5.bungee.api.ProxyServer
import java.util.logging.Filter
import java.util.logging.LogRecord

object LoggerManager : Filter {
    private val logger: MutableList<ILogger>
    private val plugin = FilterPlugin.getPlugin()
    private val schedule = ProxyServer.getInstance().scheduler

    init { logger = ArrayList() }

    override fun isLoggable(record: LogRecord?): Boolean {
        var loggable = true
        // 如果ILogger为空时 默认true
        if (logger.isEmpty()) return true
        logger.forEach { try { if (!it.isLoggable(record)) loggable = false } catch (ex: Exception) { MessageUtil.logWarnRaw("${it::class.java} throws an error. Ask that plugin developer using MoeFilter API") } }
        return loggable
    }

    fun registerFilter(c: ILogger) { logger.add(c) }

    fun unregisterFilter(c: ILogger) { schedule.runAsync(plugin) { val iterator = logger.iterator(); while (iterator.hasNext()) { if (iterator.next()::class.java == c::class.java) { iterator.remove() } } } }

}