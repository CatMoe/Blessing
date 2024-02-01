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


import com.typesafe.config.Config
import com.typesafe.config.ConfigException
import net.miaomoe.blessing.config.AbstractConfig
import net.miaomoe.blessing.config.ConfigUtil
import net.miaomoe.blessing.config.annotation.Relocated
import net.miaomoe.blessing.config.parser.DefaultConfigParser
import java.lang.reflect.InvocationTargetException

val DefaultConfigReader = ConfigReader { original, config ->
    for (field in config.parsed) {
        val path = field.path
        if (original.hasPath(path)) {
            try {
                fun setValue(value: Any) { field.field[field.config]=value }
                when (val value = field.value) {
                    is List<*> -> {
                        run {
                            val list = try { original.getConfigList(path) } catch (exception: ConfigException) { null }
                            if (list.isNullOrEmpty()) {
                                setValue(original.getAnyRefList(path))
                                return@run
                            }
                            require(field.field.isAnnotationPresent(Relocated::class.java))
                            { "The target class must be specified using @Relocated. At: $path" }
                            val target = try {
                                field.field.getAnnotation(Relocated::class.java).target.java.getDeclaredConstructor()
                            } catch (exception: Exception) {
                                throw IllegalArgumentException("Target must be have a empty constructor!", exception)
                            }
                            target.isAccessible=true
                            val newList = mutableListOf<Any>()
                            for (it in list) {
                                val newConfig = target.newInstance() as AbstractConfig
                                newConfig.let(DefaultConfigParser::parse)
                                ConfigUtil.READER.read(it, newConfig)
                                newList.add(newConfig)
                            }
                            setValue(newList)
                        }
                    }
                    is String -> setValue(original.getString(path))
                    is Int -> setValue(original.getInt(path))
                    is Long -> setValue(original.getLong(path))
                    is Boolean -> setValue(original.getBoolean(path))
                    is Double -> setValue(original.getDouble(path))
                    is AbstractConfig -> ReaderUtil.parseAndRead(original, path, value)
                    is Enum<*> -> {
                        val configValue = original.getAnyRef(path).toString().uppercase()
                        value::class.java.let {
                            try {
                                setValue(it.getMethod("valueOf", String::class.java).invoke(null, configValue))
                            } catch (exception: InvocationTargetException) {
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

internal object ReaderUtil {
    fun parseAndRead(original: Config, path: String, config: AbstractConfig) {
        ConfigUtil.PARSER.parse(config)
        ConfigUtil.READER.read(original.getConfig(path), config)
    }
}