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

package catmoe.fallencrystal.moefilter.network.limbo.block.blocks

import catmoe.fallencrystal.moefilter.network.limbo.block.LimboBlock
import catmoe.fallencrystal.translation.utils.version.Version
import com.github.benmanes.caffeine.cache.Caffeine

class Barrier : LimboBlock {
    override fun name() = "barrier"

    override fun height() = 1.0

    override fun getId(version: Version) = cache.getIfPresent(version) ?: throw IllegalArgumentException("Unknown block id for protocol")

    companion object {
        val cache = Caffeine.newBuilder().build<Version, Int>()
        init {
            for (version in Version.entries) {
                cache.put(version, when {
                    version == Version.V1_7_6 -> 20 // Glass for 1.7
                    version.fromTo(Version.V1_8, Version.V1_12_2) -> 166
                    version.fromTo(Version.V1_13, Version.V1_13_1) -> 6493
                    version == Version.V1_13_2 -> 6494
                    version.fromTo(Version.V1_14, Version.V1_15_2) -> 7000
                    version.fromTo(Version.V1_16, Version.V1_16_1) -> 7536
                    version.fromTo(Version.V1_16_2, Version.V1_16_4) -> 7540
                    version.fromTo(Version.V1_17, Version.V1_18_2) -> 7754
                    version.fromTo(Version.V1_19, Version.V1_19_1) -> 8245
                    version == Version.V1_19_3 -> 9889
                    version == Version.V1_19_4 -> 10221
                    version == Version.V1_20 -> 10225
                    version == Version.V1_20_2 -> 10366
                    else -> 0
                })
            }
        }
    }
}