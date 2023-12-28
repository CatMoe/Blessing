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
import net.kyori.adventure.nbt.BinaryTagTypes
import net.kyori.adventure.nbt.CompoundBinaryTag
import net.kyori.adventure.nbt.ListBinaryTag
import net.miaomoe.blessing.nbt.NbtUtil.put
import net.miaomoe.blessing.nbt.NbtUtil.toNamed
import net.miaomoe.blessing.nbt.NbtUtil.toNbt
import net.miaomoe.blessing.nbt.TagProvider
import net.miaomoe.blessing.nbt.chat.MixedComponent
import net.miaomoe.blessing.nbt.damage.DamageTags

@Suppress("SpellCheckingInspection")
data class Dimension(
    val key: String,
    val dimensionId: Int,
    val id: Int,
    val piglinSafe: Boolean,
    val natural: Boolean,
    val ambientLight: Float,
    val infiniburn: String,
    val respawnAnchorWorks: Boolean,
    val hasSkylight: Boolean,
    val bedWorks: Boolean,
    val effects: String,
    val hasRaids: Boolean,
    val monsterSpawnLightLevel: Int,
    val monsterSpawnBlockLightLimit: Int,
    val logicalHeight: Int,
    val coordinateScale: Float,
    val ultrawarm: Boolean,
    val hasCeiling: Boolean,
    val minY: Int,
    val height: Int,
    val biomes: List<Biome>
) : TagProvider {
    override fun toTag(version: NbtVersion?): BinaryTag {
        require(version != null) { "NbtVersion must not be null!" }
        val attributes = CompoundBinaryTag
            .builder()
            if (version == NbtVersion.LEGACY) attributes.put("name", key)
        attributes
            .put("natural", natural)
            .put("has_skylight", hasSkylight)
            .put("has_ceiling", hasCeiling)
        if (version == NbtVersion.LEGACY) {
            attributes
                .put("fixed_time", (10_000).toLong().toNbt())
                .put("shrunk", false)
        }
        attributes
            .put("ambient_light", ambientLight)
            .put("ultrawarm", ultrawarm)
            .put("has_raids", hasRaids)
            .put("respawn_anchor_works", respawnAnchorWorks)
            .put("bed_works", bedWorks)
            .put("piglin_safe", piglinSafe)
            .put("infiniburn", if (version.moreOrEqual(NbtVersion.V1_18_2)) "#$infiniburn" else infiniburn)
            .put("logical_height", logicalHeight.toByte().toNbt())
        if (version.moreOrEqual(NbtVersion.V1_16_2)) {
            attributes
                .put("effects", effects)
                .put("coordinate_scale", coordinateScale)
        }
        attributes
            .put("height", height)
            .put("min_y", minY)
        if (version == NbtVersion.LEGACY) {
            return CompoundBinaryTag.builder().put("dimension", ListBinaryTag.listBinaryTag(
                BinaryTagTypes.COMPOUND, listOf(attributes.build()))
            ).build().toNamed()
        }
        val root = CompoundBinaryTag
            .builder()
            .put("minecraft:dimension_type", CompoundBinaryTag
                .builder()
                .put("type", "minecraft:dimension_type")
                .put("value", ListBinaryTag.listBinaryTag(BinaryTagTypes.COMPOUND, listOf(
                    CompoundBinaryTag
                        .builder()
                        .put("name", key)
                        .put("id", id)
                        .put("element", attributes.build())
                        .build()
                )))
                .build()
            )
            .put("minecraft:worldgen/biome", (World.entries.firstOrNull { it.dimension == this } ?: World.OVERWORLD).toTag(version))
        if (version.moreOrEqual(NbtVersion.V1_19_4))
            root.put("minecraft:damage_type", DamageTags.getFromVersion(version).tag)
        if (version.moreOrEqual(NbtVersion.V1_19))
            root.put("minecraft:chat_type", MixedComponent.Registry.toTag(version))
        return if (version.moreOrEqual(NbtVersion.V1_20_2)) root.build() else root.build().toNamed()
    }
}
