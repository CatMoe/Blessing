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
import net.miaomoe.blessing.config.AbstractConfig
import net.miaomoe.blessing.config.ConfigUtil
import net.miaomoe.blessing.config.annotation.Relocated
import java.lang.reflect.Constructor
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.ParameterizedType

val DefaultConfigReader = ConfigReader { original, config ->
    for (field in config.parsed) {
        val path = field.path
        if (original.hasPath(path)) {
            try {
                fun setValue(value: Any) { field.field[field.config]=value }
                when (val value = field.value) {
                    is List<*> -> {
                        val listClass = try {
                            field.field.getAnnotation(Relocated::class.java)
                                ?.target?.java
                                ?: (field.field.genericType as? ParameterizedType)!!
                                .actualTypeArguments
                                .takeUnless { it?.size != 1 }!!
                                .let { it[0] as Class<*> }
                        } catch (exception: Exception) {
                            throw IllegalArgumentException("Failed to get list's generic type.", exception)
                        }
                        if (AbstractConfig::class.java.isAssignableFrom(listClass)) {
                            val constructor: Constructor<*>
                            try {
                                constructor = listClass.getDeclaredConstructor()
                                constructor.isAccessible=true
                            } catch (exception: Exception) {
                                throw IllegalArgumentException("Target must be have a empty constructor!", exception)
                            }
                            val configList = mutableListOf<Any>()
                            for (it in original.getConfigList(path)) {
                                val subConfig = constructor.newInstance() as AbstractConfig
                                ReaderUtil.parseAndRead(it, null, subConfig)
                                configList.add(subConfig)
                            }
                            setValue(configList)
                        } else setValue(original.getAnyRefList(path))
                    }
                    is String -> setValue(original.getString(path))
                    is Int -> setValue(original.getInt(path))
                    is Long -> setValue(original.getLong(path))
                    is Boolean -> setValue(original.getBoolean(path))
                    is Double -> setValue(original.getDouble(path))
                    is AbstractConfig -> ReaderUtil.parseAndRead(original, path, value)
                    is Enum<*> -> {
                        val clazz = field.field.getAnnotation(Relocated::class.java)?.target?.java ?: value::class.java
                        val enumName = original.getString(path).uppercase()
                        try {
                            clazz.getMethod("valueOf", String::class.java).invoke(null, enumName)
                        } catch (exception: InvocationTargetException) {
                            @Suppress("SpellCheckingInspection")
                            throw IllegalArgumentException(
                                "Not found \"$enumName\" enum for ${clazz.name}. " +
                                "Please check your input (like typo or enum value is non-full uppercase). " +
                                "Available enums: ${(clazz.getMethod("vaules").invoke(null) as Array<*>).joinToString(", ")}",
                                exception
                            )
                        } catch (exception: Exception) {
                            throw IllegalArgumentException("Failed to invoke ${clazz.name}#valueOf method", exception)
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
    fun parseAndRead(original: Config, path: String?, config: AbstractConfig) {
        ConfigUtil.PARSER.parse(config)
        ConfigUtil.READER.read(path?.let { original.getConfig(path) } ?: original, config)
    }
}