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

@Suppress("SpellCheckingInspection", "unused")
enum class Biome(
    val biome: String,
    val id: Int,
    val precipitation: String,
    val depth: Float,
    val temperature: Float,
    val scale: Float,
    val downfall: Float,
    val category: String,
    val skyColor: Int,
    val waterFogColor: Int,
    val fogColor: Int,
    val waterColor: Int,
    val tickDelay: Int,
    val offset: Double,
    val blockSearchExtent: Int,
    val sound: String,
    val grassColorModifier: String?,
    val foliageColor: Int
) {
    PLAINS(
        "minecraft:plains",
        1,
        "rain",
        0.125f,
        0.8f,
        0.05f,
        0.4f,
        "plains",
        7907327,
        329011,
        12638463,
        4159204,
        6000,
        2.0,
        8,
        "minecraft:ambient.cave",
        null,
        Int.MIN_VALUE
    ),
    SWAMP(
        "minecraft:swamp",
        6,
        "rain",
        -0.2f,
        0.8f,
        0.1f,
        0.9f,
        "swamp",
        7907327,
        2302743,
        12638463,
        6388580,
        6000,
        2.0,
        8,
        "minecraft:ambient.cave",
        "swamp",
        6975545
    ),
    SWAMP_HILLS(
        "minecraft:swamp_hills",
        134,
        "rain",
        -0.1f,
        0.8f,
        0.3f,
        0.9f,
        "swamp",
        7907327,
        2302743,
        12638463,
        6388580,
        6000,
        2.0,
        8,
        "minecraft:ambient.cave",
        "swamp",
        6975545
    ),
    NETHER_WASTES(
        "minecraft:nether_wastes",
        8,
        "none",
        0.1f,
        2.0f,
        0.2f,
        0.0f,
        "nether",
        7254527,
        329011,
        3344392,
        4159204,
        6000,
        2.0,
        8,
        "minecraft:ambient.cave",
        "swamp",
        6975545
    ),
    THE_END(
        "minecraft:the_end",
        9,
        "none",
        0.1f,
        0.5f,
        0.2f,
        0.5f,
        "the_end",
        7907327,
        10518688,
        12638463,
        4159204,
        6000,
        2.0,
        8,
        "minecraft:ambient.cave",
        "swamp",
        6975545
    );
}