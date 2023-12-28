/*
 * Copyright (C) 2023-2023. CatMoe / Blessing Contributors
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

package net.miaomoe.blessing.nbt.dimension

import net.kyori.adventure.nbt.BinaryTag
import net.kyori.adventure.nbt.CompoundBinaryTag
import net.miaomoe.blessing.nbt.NbtUtil.put
import net.miaomoe.blessing.nbt.NbtUtil.toNbt
import net.miaomoe.blessing.nbt.TagProvider

@Suppress("SpellCheckingInspection")
data class Biome(
    val world: World,
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
) : TagProvider {

    override fun toTag(version: NbtVersion?): BinaryTag {
        require(version != null) { "NbtVersion must not be null!" }
        val biomeTag = CompoundBinaryTag
            .builder()
            .put("name", biome.toNbt())
            .put("id", id.toNbt())
        val element = CompoundBinaryTag
            .builder()
            .put("precipitation", precipitation)
        if (version.moreOrEqual(NbtVersion.V1_19_4)) element.put("has_precipitation", (precipitation == "none").toNbt())
        element
            .put("depth", depth)
            .put("temperature", temperature)
            .put("scale", scale)
            .put("downfall", downfall)
            .put("category", category)
        val effects = CompoundBinaryTag
            .builder()
            .put("sky_color", skyColor)
            .put("water_fog_color", waterColor)
            .put("fog_color", fogColor)
            .put("water_color", waterColor)
        grassColorModifier?.let { effects.put("grass_color_modifier", it) }
        foliageColor.takeIf { it != Int.MIN_VALUE }?.let { effects.put("foliage_color", it) }
        effects.put("mood_sound", CompoundBinaryTag
            .builder()
            .put("tick_delay", tickDelay)
            .put("offset", offset)
            .put("block_search_extent", blockSearchExtent)
            .put("sound", sound)
            .build()
        )
        element.put("effects", effects.build())
        biomeTag.put("element", element.build())
        return biomeTag.build()
    }

    enum class Type(val biome: Biome) {
        PLANINS(Biome(
            World.OVERWORLD,
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
        )),
        SWAMP(Biome(
            World.OVERWORLD,
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
        )),
        SWAMP_HILLS(Biome(
            World.OVERWORLD,
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
        )),
        NETHER_WASTES(Biome(
            World.NETHER,
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
        )),
        THE_END(Biome(
            World.THE_END,
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
        ))
    }
}
