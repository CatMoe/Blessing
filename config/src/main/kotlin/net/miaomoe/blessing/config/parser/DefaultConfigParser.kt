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

package net.miaomoe.blessing.config.parser

import net.miaomoe.blessing.config.annotation.Description
import net.miaomoe.blessing.config.annotation.Path
import net.miaomoe.blessing.config.annotation.Priority
import java.lang.reflect.Modifier

val DefaultConfigParser = ConfigParser { config ->
    val list = mutableListOf<ParsedConfig>()
    val clazz = config::class.java
    for (field in clazz.declaredFields) {
        field.isAccessible=true
        val path = field.getAnnotation(Path::class.java)
        if (path == null || Modifier.isFinal(field.modifiers)) continue
        list.add(ParsedConfig(
            config,
            field,
            field[config] ?: continue,
            path.path.ifEmpty { field.name.lowercase() },
            field.getAnnotation(Priority::class.java)?.priority ?: 0.0,
            field.getAnnotation(Description::class.java)?.description?.toList() ?: listOf()
        ))
    }
    list.sortByDescending { it.priority }
    config.parsed.let {
        it.clear()
        it.addAll(list)
    }
    return@ConfigParser list
}