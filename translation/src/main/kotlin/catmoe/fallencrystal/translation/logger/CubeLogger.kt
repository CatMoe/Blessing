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

package catmoe.fallencrystal.translation.logger

import net.kyori.adventure.text.Component
import java.util.logging.Level

@Suppress("MemberVisibilityCanBePrivate")
object CubeLogger {

    var debug = false
    var logger: ICubeLogger? = null
        get() = field ?: throw NullPointerException("Logger has not been initialized!")

    fun log(level: Level, message: String) { logger!!.log(level, message) }

    fun log(level: Level, component: Component) { logger!!.log(level, component) }

    fun getInstance(): Any? { return logger?.logInstance() }

    fun debug(message: String) { if (debug) logger!!.log(Level.INFO, message) }

}