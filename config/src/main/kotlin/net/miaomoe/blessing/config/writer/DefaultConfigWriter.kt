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

package net.miaomoe.blessing.config.writer


import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigRenderOptions
import com.typesafe.config.ConfigValueFactory
import java.io.File

val DefaultConfigWriter = ConfigWriter { folder, config ->
    if (!folder.exists()) folder.mkdirs()
    val file = File(folder, "${config.name()}.conf")
    if (file.exists() || config.parsed.isEmpty()) return@ConfigWriter
    file.createNewFile()
    val map = mutableMapOf<String, Any>()
    for (info in config.parsed) {
        val description = info.description.joinToString("\n")
        val fixPrefixSpace = !info.path.contains(".")
        fun getRef(value: Any, description: String) = ConfigValueFactory.fromAnyRef(value,
            description
                .takeIf { it.isNotEmpty() }
                ?.let { if (fixPrefixSpace) "<|$it" else it }
        )
        map[info.path] = when (val value = info.value) {
            is List<*> -> getRef(value.mapNotNull { ConfigValueFactory.fromAnyRef(it) }, description)
            is Map<*, *> -> continue // Unsupported now.
            is Enum<*> -> getRef(value.name, description)
            else -> getRef(value, description)
        }
    }
    val text = ConfigFactory.parseMap(map).root().render(
        ConfigRenderOptions.defaults()
            .setOriginComments(true)
            .setFormatted(true)
            .setJson(false)
            .setComments(false)
            .setShowEnvVariableValues(true)
    )
        .replace(Regex.regex1, "")
        .replace(Regex.regex2, "# ")
        .removePrefix("\n")
    file.writeText(text)
}

internal object Regex {
    val regex1 = Regex("""\s*# hardcoded value""")
    val regex2 = Regex("\\h*(# <\\|)")
}