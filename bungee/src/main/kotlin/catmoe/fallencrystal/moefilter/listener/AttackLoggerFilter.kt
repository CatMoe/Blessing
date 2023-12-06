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

package catmoe.fallencrystal.moefilter.listener

import catmoe.fallencrystal.moefilter.api.logger.ILogger
import catmoe.fallencrystal.moefilter.common.state.StateManager
import java.util.logging.LogRecord

class AttackLoggerFilter : ILogger {

    override fun isLoggable(record: LogRecord?, isCancelled: Boolean): Boolean {
        val message = record?.message
        return if (isCancelled || message == null) false else
            !(StateManager.inAttack.get() && (message.contains("{0} has connected") || message.contains("{0} has pinged")))
    }
}