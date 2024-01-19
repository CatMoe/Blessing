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
import net.miaomoe.blessing.nbt.NbtUtil.toListTag
import net.miaomoe.blessing.nbt.NbtUtil.toNamed
import net.miaomoe.blessing.nbt.NbtUtil.toNbt
import net.miaomoe.blessing.nbt.TagProvider
import net.miaomoe.blessing.nbt.chat.ChatRegistry
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

    private val cachedTag by lazy { NbtVersion.entries.associateWith(::toTag) }
    private val cachedAttributes = NbtVersion.entries.associateWith(::getAttributes)

    fun getAttributes(version: NbtVersion): CompoundBinaryTag {
        cachedAttributes[version]?.let { return it }
        val attributes = CompoundBinaryTag
            .builder()
            .put("name", this.key)
            .put("natural", this.natural)
            .put("has_skylight", this.hasSkylight)
            .put("has_ceiling", this.hasCeiling)
            .put("fixed_time", (10000L).toNbt())
            .put("shrunk", (0).toByte().toNbt())
            .put("ambient_light", this.ambientLight)
            .put("ultrawarm", this.ultrawarm)
            .put("has_raids", this.hasRaids)
            .put("respawn_anchor_works", this.respawnAnchorWorks)
            .put("bed_works", this.bedWorks)
            .put("piglin_safe", this.piglinSafe)
            .put("infiniburn", if (version.moreOrEqual(NbtVersion.V1_18_2)) "#${this.infiniburn}" else this.infiniburn)
            .put("logical_height", this.logicalHeight)
        if (version == NbtVersion.LEGACY) {
            attributes
                .remove("name")
                .remove("fixed_time")
                .remove("shrunk")
                .put("effects", this.effects)
                .put("coordinate_scale", this.coordinateScale)
        }
        attributes
            .put("height", this.height)
            .put("min_y", this.minY)
        if (version.moreOrEqual(NbtVersion.V1_19))
            attributes
                .put("monster_spawn_light_level", this.monsterSpawnLightLevel)
                .put("monster_spawn_block_light_limit", this.monsterSpawnBlockLightLimit)
        return attributes.build()
    }

    override fun toTag(version: NbtVersion?): BinaryTag {
        require(version != null) { "NbtVersion cannot be null!" }
        cachedTag[version]?.let { return it }
        val rootCompound = CompoundBinaryTag.builder()

        return if (version == NbtVersion.LEGACY) {
            rootCompound.put("dimension", getAttributes(version).toListTag()).build().toNamed()
        } else {
            val dimensionTypeName = "minecraft:dimension_type"
            val biomeTypeName = "minecraft:worldgen/biome"
            val dimension = CompoundBinaryTag
                .builder()
                .put("type", dimensionTypeName)
                .put("value",
                    CompoundBinaryTag.builder()
                        .put("name", this.key)
                        .put("id", this.id)
                        .put("element", getAttributes(version))
                        .build()
                        .toListTag()
                )
                .build()
            val biome = CompoundBinaryTag
                .builder()
                .put("type", biomeTypeName)
                .put("value", biomes.map { it.toTag(version) as CompoundBinaryTag }.toListTag())
                .build()
            rootCompound
                .put(dimensionTypeName, dimension)
                .put(biomeTypeName, biome)
            if (version.moreOrEqual(NbtVersion.V1_19_4)) {
                val damageTags = if (version.moreOrEqual(NbtVersion.V1_20)) DamageTags.V1_20 else DamageTags.V1_19
                rootCompound.put("minecraft:damage_type", damageTags.toTag(version))
            }
            if (version.moreOrEqual(NbtVersion.V1_19)) rootCompound.put("minecraft:chat_type", ChatRegistry.toTag(version))
            rootCompound.build().let { if (version.moreOrEqual(NbtVersion.V1_20_2)) it else it.toNamed() }
        }
    }
}
