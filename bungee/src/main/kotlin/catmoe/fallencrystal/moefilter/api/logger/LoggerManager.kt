/*
 * Copyright (C) 2023-2023. CatMoe / MoeFilter Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package catmoe.fallencrystal.moefilter.api.logger

import catmoe.fallencrystal.moefilter.util.message.v2.MessageUtil
import catmoe.fallencrystal.moefilter.util.plugin.util.Scheduler
import java.util.concurrent.CopyOnWriteArrayList
import java.util.logging.Filter
import java.util.logging.LogRecord

@Suppress("unused")
object LoggerManager : Filter {
    private val logger: MutableList<ILogger> = CopyOnWriteArrayList()

    private var logType: BCLogType? = null

    override fun isLoggable(record: LogRecord?): Boolean {
        var loggable = true
        // 如果ILogger为空时 默认true
        if (logger.isEmpty()) return true
        if (record?.message?.startsWith("[MoeFilter]") == true) return true
        // logger.forEach { try { if (!it.isLoggable(record)) loggable = false } catch (ex: Exception) { MessageUtil.logWarnRaw("${it::class.java} throws an error. Ask that plugin developer using MoeFilter API") } }
        for (it in logger) {
            try {
                if (!it.isLoggable(record, !loggable)) { loggable = false }
            } catch (ex: Exception) { MessageUtil.logWarn("[MoeFilter] [Logger] ${it::class.java} throw an error. Ask that plugin developer what they using MoeFilter API."); ex.printStackTrace() }
        }
        return loggable
    }

    fun registerFilter(c: ILogger) { logger.add(c) }

    @Suppress("UNUSED")
    fun unregisterFilter(c: ILogger) {
        Scheduler.getDefault().runAsync {
            val removeLogger: MutableList<ILogger> = ArrayList()
            for (it in logger) { if (it::class.java == c::class.java) { removeLogger.add(it); break; } }
            logger.removeAll(removeLogger.toSet())
        }
    }

    fun getType(): BCLogType? { return logType }

    fun setType(logType: BCLogType) { this.logType=logType }
}