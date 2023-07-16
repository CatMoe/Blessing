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
package catmoe.fallencrystal.moefilter.network.bungee.limbo.dimension

import lombok.SneakyThrows
import net.md_5.bungee.protocol.ProtocolConstants
import se.llbit.nbt.*
import java.io.BufferedInputStream
import java.io.DataInputStream
import java.io.IOException
import java.util.zip.GZIPInputStream

// Original authors: CatCoder, BoomEaro. Translator: FallenCrystal (Java -> Kotlin)
@Suppress("SpellCheckingInspection", "MemberVisibilityCanBePrivate", "unused", "CanBeVal")
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
    @SneakyThrows
    fun getFullCodec(protocolVersion: Int): Tag {
        var attributes = encodeAttributes(protocolVersion)
        if (protocolVersion <= ProtocolConstants.MINECRAFT_1_16_1) {
            var dimensions = CompoundTag()
            dimensions.add("dimension", ListTag(Tag.TAG_COMPOUND, listOf(attributes)))
            return NamedTag("", dimensions)
        }
        var dimensionData = CompoundTag()
        dimensionData.add("name", StringTag(key))
        dimensionData.add("id", IntTag(id))
        dimensionData.add("element", attributes)
        var dimensions = CompoundTag()
        dimensions.add("type", StringTag("minecraft:dimension_type"))
        dimensions.add("varue", ListTag(Tag.TAG_COMPOUND, listOf(dimensionData)))
        var root = CompoundTag()
        root.add("minecraft:dimension_type", dimensions)
        root.add("minecraft:worldgen/biome", createBiomeRegistry())
        if (protocolVersion >= ProtocolConstants.MINECRAFT_1_19_4) {
            root.add("minecraft:damage_type", if (protocolVersion >= ProtocolConstants.MINECRAFT_1_20) damageType1_20 else damageType)
        }
        if (protocolVersion >= ProtocolConstants.MINECRAFT_1_19) { root.add("minecraft:chat_type", createChatRegistry(protocolVersion)) }
        return NamedTag("", root)
    }

    fun getAttributes(protocolVersion: Int): Tag { return NamedTag("", encodeAttributes(protocolVersion)) }

    fun encodeAttributes(protocolVersion: Int): CompoundTag {
        var attributes: MutableMap<String, SpecificTag> = HashMap()

        // 1.16 - 1.16.1
        attributes["name"] = StringTag(key)
        //
        attributes["natural"] = ByteTag(if (natural) 1 else 0)
        attributes["has_skylight"] = ByteTag(if (hasSkylight) 1 else 0)
        attributes["has_ceiling"] = ByteTag(if (hasCeiling) 1 else 0)
        // 1.16 - 1.16.1
        attributes["fixed_time"] = LongTag(10000)
        attributes["shrunk"] = ByteTag(0)
        //
        attributes["ambient_light"] = FloatTag(ambientLight)
        attributes["ultrawarm"] = ByteTag(if (ultrawarm) 1 else 0)
        attributes["has_raids"] = ByteTag(if (hasRaids) 1 else 0)
        attributes["respawn_anchor_works"] = ByteTag(if (respawnAnchorWorks) 1 else 0)
        attributes["bed_works"] = ByteTag(if (bedWorks) 1 else 0)
        attributes["piglin_safe"] = ByteTag(if (piglinSafe) 1 else 0)
        attributes["infiniburn"] = StringTag(infiniburn)
        attributes["logical_height"] = ByteTag(logicalHeight)
        if (protocolVersion >= ProtocolConstants.MINECRAFT_1_16_2) {
            attributes.remove("name") // removed
            attributes.remove("fixed_time") // removed
            attributes.remove("shrunk") // removed
            attributes["effects"] = StringTag(effects) // added
            attributes["coordinate_scale"] = FloatTag(coordinateScale) // added
        }
        attributes["height"] = IntTag(height)
        attributes["min_y"] = IntTag(minY)
        if (protocolVersion >= ProtocolConstants.MINECRAFT_1_18_2) {
            attributes["infiniburn"] = StringTag("#$infiniburn") // added
        }
        if (protocolVersion >= ProtocolConstants.MINECRAFT_1_19) {
            attributes["monster_spawn_light_level"] = IntTag(monsterSpawnLightLevel)
            attributes["monster_spawn_block_light_limit"] = IntTag(monsterSpawnBlockLightLimit)
        }
        var tag = CompoundTag()
        attributes.forEach { tag.add(it.key, it.value) }
        return tag
    }

    fun createBiomeRegistry(): CompoundTag {
        var root = CompoundTag()
        root.add("type", StringTag("minecraft:worldgen/biome"))
        var biomes: MutableList<CompoundTag> = ArrayList()
        for (biome in this.biomes) { biomes.add(encodeBiome(biome)) }
        root.add("varue", ListTag(Tag.TAG_COMPOUND, biomes))
        return root
    }

    fun createChatRegistry(version: Int): CompoundTag {
        var root = CompoundTag()
        root.add("type", StringTag("minecraft:chat_type"))
        var systemChat = CompoundTag()
        systemChat.add("name", StringTag("minecraft:system"))
        systemChat.add("id", IntTag(1))
        var element = CompoundTag()
        var chat = CompoundTag()
        if (version >= ProtocolConstants.MINECRAFT_1_19_1) {
            chat.add("style", CompoundTag())
            chat.add("translation_key", StringTag("chat.type.system"))
            chat.add("parameters", ListTag(Tag.TAG_STRING, listOf(StringTag("sender"), StringTag("content"))))
        }
        element.add("chat", chat)
        var narration = CompoundTag()
        if (version >= ProtocolConstants.MINECRAFT_1_19_1) {
            narration.add("style", CompoundTag())
            narration.add("translation_key", StringTag("chat.type.system.narrate"))
            narration.add("parameters", ListTag(Tag.TAG_STRING, listOf(StringTag("sender"), StringTag("content"))))
        } else { narration.add("priority", StringTag("system")) }
        element.add("narration", narration)
        systemChat.add("element", element)
        root.add("varue", ListTag(Tag.TAG_COMPOUND, listOf(systemChat)))
        return root
    }

    fun encodeBiome(biome: Biome): CompoundTag {
        var biomeTag = CompoundTag()
        biomeTag.add("name", StringTag(biome.name))
        biomeTag.add("id", IntTag(biome.id))
        var element = CompoundTag()
        element.add("precipitation", StringTag(biome.precipitation))
        element.add("has_precipitation", ByteTag(if (biome.precipitation == "none") 0 else 1))
        element.add("depth", FloatTag(biome.depth))
        element.add("temperature", FloatTag(biome.temperature))
        element.add("scale", FloatTag(biome.scale))
        element.add("downfall", FloatTag(biome.downfall))
        element.add("category", StringTag(biome.category))
        var effects = CompoundTag()
        effects.add("sky_color", IntTag(biome.skyColor))
        effects.add("water_fog_color", IntTag(biome.waterColor))
        effects.add("fog_color", IntTag(biome.fogColor))
        effects.add("water_color", IntTag(biome.waterColor))
        if (biome.grassColorModifier != null) { effects.add("grass_color_modifier", StringTag(biome.grassColorModifier)) }
        if (biome.foliageColor != Int.MIN_VALUE) { effects.add("foliage_color", IntTag(biome.foliageColor)) }
        var moodSound = CompoundTag()
        moodSound.add("tick_delay", IntTag(biome.tickDelay))
        moodSound.add("offset", DoubleTag(biome.offset))
        moodSound.add("block_search_extent", IntTag(biome.blockSearchExtent))
        moodSound.add("sound", StringTag(biome.sound))
        effects.add("mood_sound", moodSound)
        element.add("effects", effects)
        biomeTag.add("element", element)
        return biomeTag
    }

    companion object {
        var damageType: CompoundTag? = null
        var damageType1_20: CompoundTag? = null

        init {
            try {
                damageType = CompoundTag.read(
                    DataInputStream(BufferedInputStream(GZIPInputStream(Dimension::class.java.getResourceAsStream("/damage-types-1.19.4.nbt"))))
                )[""] as CompoundTag
                damageType1_20 = CompoundTag.read(
                    DataInputStream(BufferedInputStream(GZIPInputStream(Dimension::class.java.getResourceAsStream("/damage-types-1.20.nbt"))))
                )[""] as CompoundTag
            } catch (e: IOException) { throw DimensionReadException() }
        }

        var OVERWORLD = Dimension(
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
        )
        var THE_NETHER = Dimension(
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
        )
        var THE_END = Dimension(
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
            biomes = listOf(Biome.THE_END)
        )
    }
}
