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

import catmoe.fallencrystal.moefilter.network.limbo.dimension.llbit.StaticDimension.d1
import catmoe.fallencrystal.moefilter.network.limbo.dimension.llbit.StaticDimension.d2
import catmoe.fallencrystal.translation.utils.version.Version
import se.llbit.nbt.*
import java.util.*

@Suppress("SpellCheckingInspection", "MemberVisibilityCanBePrivate")
class Dimension(
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
) {

    fun getFullCodec(version: Version): Tag {
        val attributes = encodeAttributes(version)
        val data = CompoundTag()
        if (version.lessOrEqual(Version.V1_16_1)) {
            data.add("dimension", ListTag(Tag.TAG_COMPOUND /* 10 */, listOf(attributes)))
            return NamedTag("", data)
        }
        data.add("name", StringTag(key))
        data.add("id", IntTag(id))
        data.add("element", attributes)
        val d = CompoundTag()
        d.add("type", StringTag("minecraft:dimension_type"))
        d.add("value", ListTag(Tag.TAG_COMPOUND, Collections.singletonList(data)))
        val root = CompoundTag()
        root.add("minecraft:dimension_type", d)
        root.add("minecraft:worldgen/biome", createBiomeRegistry(version))
        if (version.moreOrEqual(Version.V1_19_4)) root.add("minecraft:damage_type", if (version == Version.V1_19_4) d1 else d2)
        if (version.moreOrEqual(Version.V1_19)) root.add("minecraft:chat_type", createChatRegistry(version))
        return if (version.moreOrEqual(Version.V1_20_2)) root else NamedTag("", root)
    }

    fun getAttributes(version: Version): Tag { return NamedTag("", encodeAttributes(version)) }

    fun encodeAttributes(version: Version): CompoundTag {
        val attributes: MutableMap<String, SpecificTag> = HashMap()

        attributes["name"] = StringTag(key)
        attributes["natural"] = ByteTag(if (natural) 1 else 0)
        attributes["has_skylight"] = ByteTag(if (hasSkylight) 1 else 0)
        attributes["has_ceiling"] = ByteTag(if (hasCeiling) 1 else 0)
        attributes["fixed_time"] = LongTag(10_000)
        attributes["shrunk"] = ByteTag(0)
        attributes["ambient_light"] = FloatTag(ambientLight)
        attributes["ultrawarm"] = ByteTag(if (ultrawarm) 1 else 0)
        attributes["has_raids"] = ByteTag(if (hasRaids) 1 else 0)
        attributes["respawn_anchor_works"] = ByteTag(if (respawnAnchorWorks) 1 else 0)
        attributes["bed_works"] = ByteTag(if (bedWorks) 1 else 0)
        attributes["piglin_safe"] = ByteTag(if (piglinSafe) 1 else 0)
        attributes["infiniburn"] = StringTag(infiniburn)
        attributes["logical_height"] = ByteTag(logicalHeight)
        if (version.moreOrEqual(Version.V1_16_2)) {
            attributes.remove("name")
            attributes.remove("fixed_time")
            attributes.remove("shrunk")
            attributes["effects"] = StringTag(effects)
            attributes["coordinate_scale"] = FloatTag(coordinateScale)
        }
        attributes["height"] = IntTag(height)
        attributes["min_y"] = IntTag(minY)
        if (version.moreOrEqual(Version.V1_18_2)) attributes["infiniburn"] = StringTag("#$infiniburn")
        if (version.moreOrEqual(Version.V1_19)) {
            attributes["monster_spawn_light_level"] = IntTag(monsterSpawnLightLevel)
            attributes["monster_spawn_block_light_limit"] = IntTag(monsterSpawnBlockLightLimit)
        }
        val tag = CompoundTag()
        attributes.forEach { tag.add(it.key, it.value) }
        return tag
    }

    fun createBiomeRegistry(version: Version): CompoundTag {
        val root = CompoundTag()
        root.add("type", StringTag("minecraft:worldgen/biome"))
        val biomes: MutableList<CompoundTag> = ArrayList()
        for (biome in this.biomes) { biomes.add(encodeBiome(biome, version)) }
        root.add("value", ListTag(Tag.TAG_COMPOUND, biomes))
        return root
    }

    fun encodeBiome(biome: Biome, version: Version): CompoundTag {
        val biomeTag = CompoundTag()
        biomeTag.add("name", StringTag(biome.biome))
        biomeTag.add("id", IntTag(biome.id))
        val element = CompoundTag()
        element.add("precipitation", StringTag(biome.precipitation))
        if (version.moreOrEqual(Version.V1_19_4)) element.add("has_precipitation", ByteTag(if (biome.precipitation == "none") 0 else 1))
        element.add("depth", FloatTag(biome.depth))
        element.add("temperature", FloatTag(biome.temperature))
        element.add("scale", FloatTag(biome.scale))
        element.add("downfall", FloatTag(biome.downfall))
        element.add("category", StringTag(biome.category))
        val effects = CompoundTag()
        effects.add("sky_color", IntTag(biome.skyColor))
        effects.add("water_fog_color", IntTag(biome.waterColor))
        effects.add("fog_color", IntTag(biome.fogColor))
        effects.add("water_color", IntTag(biome.waterColor))
        if (biome.grassColorModifier != null) { effects.add("grass_color_modifier", StringTag(biome.grassColorModifier)) }
        if (biome.foliageColor != Int.MIN_VALUE) { effects.add("foliage_color", IntTag(biome.foliageColor)) }
        val moodSound = CompoundTag()
        moodSound.add("tick_delay", IntTag(biome.tickDelay))
        moodSound.add("offset", DoubleTag(biome.offset))
        moodSound.add("block_search_extent", IntTag(biome.blockSearchExtent))
        moodSound.add("sound", StringTag(biome.sound))
        effects.add("mood_sound", moodSound)
        element.add("effects", effects)
        biomeTag.add("element", element)
        return biomeTag
    }

    fun createChatRegistry(version: Version): CompoundTag {
        val root = CompoundTag()
        root.add("type", StringTag("minecraft:chat_type"))
        val systemChat = CompoundTag()
        systemChat.add("name", StringTag("minecraft:system"))
        systemChat.add("id", IntTag(1))
        val element = CompoundTag()
        val chat = CompoundTag()
        if (version.moreOrEqual(Version.V1_19_1)) {
            chat.add("style", CompoundTag())
            chat.add("translation_key", StringTag("chat.type.system"))
            chat.add("parameters", ListTag(Tag.TAG_STRING, listOf(StringTag("sender"), StringTag("content"))))
        }
        element.add("chat", chat)
        val narration = CompoundTag()
        if (version.moreOrEqual(Version.V1_19_1)) {
            narration.add("style", CompoundTag())
            narration.add("translation_key", StringTag("chat.type.system.narrate"))
            narration.add("parameters", ListTag(Tag.TAG_STRING, listOf(StringTag("sender"), StringTag("content"))))
        } else { narration.add("priority", StringTag("system")) }
        element.add("narration", narration)
        systemChat.add("element", element)
        root.add("value", ListTag(Tag.TAG_COMPOUND, listOf(systemChat)))
        return root
    }

}