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

package catmoe.fallencrystal.moefilter.network.limbo.dimension.llbit

@Suppress("SpellCheckingInspection")
enum class DimensionType(@JvmField val dimension: Dimension) {
    OVERWORLD(Dimension(
        "minecraft:overworld", 0, 0,
        piglinSafe = false, natural = true, ambientLight = 0.0f,
        infiniburn = "minecraft:infiniburn_overworld",
        respawnAnchorWorks = false, hasSkylight = true,
        bedWorks = true, effects = "minecraft:overworld",
        hasRaids = true, monsterSpawnLightLevel = 0,
        monsterSpawnBlockLightLimit = 0, logicalHeight = 256,
        coordinateScale = 1.0f, ultrawarm = false,
        hasCeiling = false, minY = 0, height = 256,
        biomes = listOf(Biome.PLAINS, Biome.SWAMP, Biome.SWAMP_HILLS)
    )),
    NETHER(Dimension(
        "minecraft:the_nether", -1, 2,
        piglinSafe = false, natural = true, ambientLight = 0.0f,
        infiniburn = "minecraft:infiniburn_nether",
        respawnAnchorWorks = false, hasSkylight = true,
        bedWorks = true, effects = "minecraft:the_nether",
        hasRaids = true, monsterSpawnLightLevel = 0,
        monsterSpawnBlockLightLimit = 0, logicalHeight = 256,
        coordinateScale = 1.0f, ultrawarm = false,
        hasCeiling = false, minY = 0, height = 256,
        biomes = listOf(Biome.NETHER_WASTES)
    )),
    THE_END(Dimension(
        "minecraft:the_end", 1, 3,
        piglinSafe = false, natural = true, ambientLight = 0.0f,
        infiniburn = "minecraft:infiniburn_end",
        respawnAnchorWorks = false, hasSkylight = true,
        bedWorks = true, effects = "minecraft:the_end",
        hasRaids = true, monsterSpawnLightLevel = 0,
        monsterSpawnBlockLightLimit = 0, logicalHeight = 256,
        coordinateScale = 1.0f, ultrawarm = false,
        hasCeiling = false, minY = 0, height = 256,
        biomes = listOf(Biome.THE_END))
    )
}