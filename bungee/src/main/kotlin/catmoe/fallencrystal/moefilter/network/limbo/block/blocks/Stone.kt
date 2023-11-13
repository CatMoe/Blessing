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

class Stone : LimboBlock {

    override fun name() = "stone"

    override fun height() = 1.0

    override fun getId(version: Version) = cache.getIfPresent(version)!!

    companion object {

        private val cache = Caffeine.newBuilder().build<Version, Int>()
        init {
            for (version in Version.entries) cache.put(version, if (version.moreOrEqual(Version.V1_13)) 16 else 1)
        }
    }

}