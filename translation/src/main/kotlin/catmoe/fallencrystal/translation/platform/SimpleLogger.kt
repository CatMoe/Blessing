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

package catmoe.fallencrystal.translation.platform

import java.util.logging.Level

class SimpleLogger(val logger: Any) {

    fun log(level: Level, message: String) {
        try {
            if (logger is java.util.logging.Logger) { logger.log(level, message); return }
            else if (logger is org.slf4j.Logger) {
                when (level) {
                    Level.INFO -> logger.info(message)
                    Level.WARNING -> logger.info(message)
                    Level.SEVERE -> logger.error(message)
                    else -> logger.info(message)
                }
            }
        } catch (_: NoClassDefFoundError) { }
    }

}