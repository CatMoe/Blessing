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
import com.typesafe.config.ConfigValueFactory.fromAnyRef
import net.miaomoe.blessing.config.AbstractConfig
import net.miaomoe.blessing.config.hook.ReplaceHook
import net.miaomoe.blessing.config.parser.DefaultConfigParser

val DefaultConfigWriter = ConfigWriter { file, config ->
    val map = mutableMapOf<String, Any>()
    Helper.write(map, config)
    val text = ConfigFactory.parseMap(map).root().render(
        ConfigRenderOptions.defaults()
            .setOriginComments(true)
            .setFormatted(true)
            .setJson(false)
            .setComments(false)
            .setShowEnvVariableValues(true)
    )
        .replace(Helper.regex1, "")
        .replace(Helper.regex2, "# ")
        .removePrefix("\n")
    file.writeText(text)
}
internal object Helper {

    val regex1 = Regex("""\s*# hardcoded value""")
    val regex2 = Regex("\\h*(# <\\|)")

    @JvmOverloads
    fun write(map: MutableMap<String, Any>, config: AbstractConfig, parent: String = "", fixPrefixSpace: Boolean = true) {
        for (info in config.parsed) write(map, "$parent${info.path}", info.value, info.description, fixPrefixSpace)
    }

    private fun write(
        map: MutableMap<String, Any>,
        path: String,
        value: Any,
        description: List<String>,
        fixPrefixSpace: Boolean
    ) {
        val prefixFixer = path.let { if (fixPrefixSpace && !path.contains(".")) "<|" else "" }
        val desc =
            description
                .takeIf { it.isNotEmpty() }
                ?.joinToString("\n$prefixFixer")
                ?.let { "$prefixFixer$it" }
                ?.let(ReplaceHook::replace)
        val v: Any = when (value) {
            is List<*> -> {
                val values = mutableListOf<MutableMap<String, Any>>()
                for (it in value) {
                    it as? AbstractConfig ?: continue
                    val valuesMap = mutableMapOf<String, Any>()
                    it
                        .let(DefaultConfigParser::parse)
                        .forEach { write(valuesMap, it.path, it.value, it.description, false) }
                    values.add(valuesMap)
                }
                if (values.isNotEmpty()) {
                    map[path] = values
                    return
                } else value.mapNotNull { fromAnyRef(it) }
            }
            is Enum<*> -> value.name
            is AbstractConfig -> {
                val parsed = value.let(DefaultConfigParser::parse)
                if (desc.isNullOrEmpty()) {
                    write(map, value, "$path.", false)
                    return
                } else {
                    val anotherMap = mutableMapOf<String, Any>()
                    parsed.forEach { write(anotherMap, it.path, it.value, it.description, false) }
                    anotherMap
                }
            }
            is Map<*, *> -> throw IllegalArgumentException("Please surround with AbstractConfig! At: $path")
            else -> value
        }
        map[path] = fromAnyRef(v, desc)
    }
}