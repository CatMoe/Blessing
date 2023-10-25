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

package catmoe.fallencrystal.moefilter.listener.main

import catmoe.fallencrystal.moefilter.api.logger.ILogger
import catmoe.fallencrystal.moefilter.common.state.StateManager
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
        val a = if (record.thrown?.cause != null) { !filterException.contains(record.thrown.cause!!::class.java.name) } else record.message?.contains("InitialHandler - encountered exception") != true
        val b = if (StateManager.inAttack.get()) record.message.contains("{0} has ") else false
        return a || b
    }
}