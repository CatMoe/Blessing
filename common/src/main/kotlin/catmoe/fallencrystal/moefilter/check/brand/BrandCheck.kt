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

package catmoe.fallencrystal.moefilter.check.brand

import catmoe.fallencrystal.moefilter.check.AbstractCheck
import catmoe.fallencrystal.moefilter.check.brand.BrandCheckMode.BLACKLIST
import catmoe.fallencrystal.moefilter.check.brand.BrandMatchType.*
import catmoe.fallencrystal.moefilter.check.info.CheckInfo
import catmoe.fallencrystal.moefilter.check.info.impl.Brand
import catmoe.fallencrystal.translation.utils.config.LocalConfig
import catmoe.fallencrystal.translation.utils.config.Reloadable

object BrandCheck : AbstractCheck(), Reloadable {

    private var config = LocalConfig.getConfig().getConfig("client-brand")
    private var a1 = ""
    private var matchType = CONTAINS
    private var matchMode = BLACKLIST
    private var list: List<String> = listOf()

    override fun reload() { this.init()  }

    fun init() {
        config = LocalConfig.getConfig().getConfig("client-brand")
        matchMode = try { BrandCheckMode.valueOf("${config.getAnyRef("mode")}") } catch (_: IllegalArgumentException) { BLACKLIST }
        matchType = try { BrandMatchType.valueOf("${config.getAnyRef("equal-mode")}") } catch (_: IllegalArgumentException) { CONTAINS }
        list = config.getStringList("list")
        if (list.size == 1) a1 = list[0]
    }

    override fun increase(info: CheckInfo): Boolean {
        info as Brand
        if (list.isEmpty()) return true
        val match = when (list.size == 1 && a1.isNotEmpty()) {
            true -> match(info.brand, a1)
            false -> {
                var m = false
                list.forEach { if (match(info.brand, it)) { m=true; return@forEach } }
                m
            }
        }
        return if (match) matchMode.ifMatch else !matchMode.ifMatch
    }

    private fun match(brand: String, target: String): Boolean {
        return when (matchType) {
            CONTAINS -> (brand.contains(target, ignoreCase = true))
            EQUAL -> brand == target
            IGNORE_CASE -> brand.equals(target, ignoreCase = true)
            REGEX -> target.toRegex().matches(brand)
        }
    }

}