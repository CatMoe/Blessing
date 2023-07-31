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

package catmoe.fallencrystal.moefilter.network.limbo.dimension.llbit

@Suppress("SpellCheckingInspection", "unused")
enum class DimensionType(@JvmField val dimension: Dimension) {
    OVERWORLD(Dimension(
        "minecraft:overworld", 0, 0,
        piglinSafe = false,
        natural = true,
        ambientLight = 0.0f,
        infiniburn = "minecraft:infiniburn_overworld",
        respawnAnchorWorks = false,
        hasSkylight = true,
        bedWorks = true,
        effects = "minecraft:overworld",
        hasRaids = true,
        monsterSpawnLightLevel = 0,
        monsterSpawnBlockLightLimit = 0,
        logicalHeight = 256,
        coordinateScale = 1.0f,
        ultrawarm = false,
        hasCeiling = false,
        minY = 0,
        height = 256,
        biomes = listOf(Biome.PLAINS, Biome.SWAMP, Biome.SWAMP_HILLS)
    )),
    NETHER(Dimension(
        "minecraft:the_nether", -1, 2,
        piglinSafe = false,
        natural = true,
        ambientLight = 0.0f,
        infiniburn = "minecraft:infiniburn_nether",
        respawnAnchorWorks = false,
        hasSkylight = true,
        bedWorks = true,
        effects = "minecraft:the_nether",
        hasRaids = true,
        monsterSpawnLightLevel = 0,
        monsterSpawnBlockLightLimit = 0,
        logicalHeight = 256,
        coordinateScale = 1.0f,
        ultrawarm = false,
        hasCeiling = false,
        minY = 0,
        height = 256,
        biomes = listOf(Biome.NETHER_WASTES)
    )),
    THE_END(Dimension(
        "minecraft:the_end", 1, 3,
        piglinSafe = false,
        natural = true,
        ambientLight = 0.0f,
        infiniburn = "minecraft:infiniburn_end",
        respawnAnchorWorks = false,
        hasSkylight = true,
        bedWorks = true,
        effects = "minecraft:the_end",
        hasRaids = true,
        monsterSpawnLightLevel = 0,
        monsterSpawnBlockLightLimit = 0,
        logicalHeight = 256,
        coordinateScale = 1.0f,
        ultrawarm = false,
        hasCeiling = false,
        minY = 0,
        height = 256,
        biomes = listOf(Biome.THE_END))
    )
}