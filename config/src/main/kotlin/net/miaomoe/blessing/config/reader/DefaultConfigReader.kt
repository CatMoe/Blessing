/*
 * Copyright (C) 2023-2024. CatMoe / Blessing Contributors
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

package net.miaomoe.blessing.config.reader


import com.typesafe.config.ConfigFactory
import java.io.File

val DefaultConfigReader = ConfigReader { folder, config ->
    val file = File(folder, "${config.name()}.conf")
    if (!file.exists() || config.parsed.isEmpty()) return@ConfigReader
    val a = ConfigFactory.parseFile(file)
    for (field in config.parsed) {
        val path = field.path
        if (a.hasPath(path)) {
            val b = field.value
            try {
                fun setValue(value: Any) { field.field[field.config]=value }
                when (b) {
                    is List<*> -> setValue(a.getAnyRefList(path))
                    is String -> setValue(a.getString(path))
                    is Int -> setValue(a.getInt(path))
                    is Long -> setValue(a.getLong(path))
                    is Boolean -> setValue(a.getBoolean(path))
                    is Double -> setValue(a.getDouble(path))
                    // Unsupported
                    else -> continue
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
                continue
            }
        }
    }
}