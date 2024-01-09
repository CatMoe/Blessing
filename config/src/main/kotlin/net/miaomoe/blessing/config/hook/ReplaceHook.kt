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

package net.miaomoe.blessing.config.hook

import java.util.function.UnaryOperator

object ReplaceHook {

    private val functions = mutableListOf<UnaryOperator<String>>()

    fun register(function: UnaryOperator<String>) {
        functions.takeUnless { it.contains(function) }?.add(function)
    }

    fun unregister(function: UnaryOperator<String>) {
        functions.remove(function)
    }

    fun replace(original: String): String {
        var output = original
        functions.forEach { function -> output = output.let(function::apply) }
        return output
    }
}