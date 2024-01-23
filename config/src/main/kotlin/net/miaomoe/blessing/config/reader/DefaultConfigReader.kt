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


import net.miaomoe.blessing.config.AbstractConfig
import net.miaomoe.blessing.config.ConfigUtil

val DefaultConfigReader = ConfigReader { original, config ->
    for (field in config.parsed) {
        val path = field.path
        if (original.hasPath(path)) {
            try {
                fun setValue(value: Any) { field.field[field.config]=value }
                when (val value = field.value) {
                    is List<*> -> setValue(original.getAnyRefList(path))
                    is String -> setValue(original.getString(path))
                    is Int -> setValue(original.getInt(path))
                    is Long -> setValue(original.getLong(path))
                    is Boolean -> setValue(original.getBoolean(path))
                    is Double -> setValue(original.getDouble(path))
                    is AbstractConfig -> {
                        ConfigUtil.PARSER.parse(value)
                        ConfigUtil.READER.read(original.getConfig(path), value)
                    }
                    is Enum<*> -> {
                        val configValue = original.getAnyRef(path).toString().uppercase()
                        value::class.java.let {
                            try {
                                setValue(it.getMethod("valueOf", String::class.java).invoke(null, configValue))
                            } catch (exception: IllegalArgumentException) {
                                @Suppress("SpellCheckingInspection")
                                throw IllegalArgumentException(
                                    "Not found \"$configValue\" enum for ${it.name}. " +
                                    "Please check your input (like typo or enum value is non-full uppercase). " +
                                    "Available enums: ${(it.getMethod("vaules").invoke(null) as Array<*>).joinToString(", ")}",
                                    exception
                                )
                            } catch (exception: Exception) {
                                throw IllegalArgumentException("Failed to invoke ${it.name}#valueOf method", exception)
                            }
                        }
                    }
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