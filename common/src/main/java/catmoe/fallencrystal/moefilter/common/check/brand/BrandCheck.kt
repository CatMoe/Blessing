/*
 * Copyright 2023. CatMoe / FallenCrystal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package catmoe.fallencrystal.moefilter.common.check.brand

import catmoe.fallencrystal.moefilter.common.check.AbstractCheck
import catmoe.fallencrystal.moefilter.common.check.brand.BrandCheckMode.BLACKLIST
import catmoe.fallencrystal.moefilter.common.check.brand.BrandMatchType.*
import catmoe.fallencrystal.moefilter.common.check.info.CheckInfo
import catmoe.fallencrystal.moefilter.common.check.info.impl.Brand
import catmoe.fallencrystal.translation.utils.config.LocalConfig

object BrandCheck : AbstractCheck() {

    private var config = LocalConfig.getConfig().getConfig("client-brand")
    private var a1 = ""
    private var matchType = CONTAINS
    private var matchMode = BLACKLIST
    private var list: List<String> = listOf()

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