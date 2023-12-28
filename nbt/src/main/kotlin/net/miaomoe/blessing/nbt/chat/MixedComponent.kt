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

package net.miaomoe.blessing.nbt.chat

import com.google.gson.*
import net.kyori.adventure.nbt.*
import net.kyori.adventure.nbt.CompoundBinaryTag.Builder
import net.miaomoe.blessing.nbt.NbtUtil.put
import net.miaomoe.blessing.nbt.NbtUtil.toNamed
import net.miaomoe.blessing.nbt.NbtUtil.toNbt
import net.miaomoe.blessing.nbt.TagProvider
import net.miaomoe.blessing.nbt.dimension.NbtVersion

class MixedComponent(
    val json: String,
    val tag: BinaryTag
) {

    constructor(json: String) : this(json, serialize(JsonParser.parseString(json)))

    object Registry : TagProvider {

        override fun toTag(version: NbtVersion?): BinaryTag {
            require(version != null) { "NbtVersion must not be null!" }
            val element = CompoundBinaryTag.builder()
            CompoundBinaryTag.builder().let {
                val tag = if (version.moreOrEqual(NbtVersion.V1_19_1)) addChatInfo(it, "chat.type.system") else it
                element.put("chat", tag.build())
            }
            CompoundBinaryTag.builder().let {
                val tag = if (version.moreOrEqual(NbtVersion.V1_19_1))
                    addChatInfo(it, "chat.type.system.narrate")
                else it.put("priority", "system")
                element.put("narration", tag.build())
            }
            return CompoundBinaryTag
                .builder()
                .put("type", "minecraft:chat_type")
                .put("value", CompoundBinaryTag.builder()
                    .put("name", "minecraft:system")
                    .put("id", 1)
                    .put("element", element.build())
                    .build()
                )
                .build()
        }

        private fun addChatInfo(builder: Builder, key: String): Builder {
            return builder
                .put("style", CompoundBinaryTag.empty())
                .put("translation_key", key)
                .put("parameters", ListBinaryTag.listBinaryTag(BinaryTagTypes.STRING, listOf("sender".toNbt(), "content".toNbt())))
        }

    }

    companion object {
        fun serialize(json: JsonElement): BinaryTag {
            when (json) {
                is JsonPrimitive -> {
                    when {
                        json.isNumber -> return json.asNumber.toNbt()
                        json.isString -> return json.asString.toNbt()
                        json.isBoolean -> json.asBoolean.toNbt()
                        else -> throw IllegalArgumentException("Unknown JSON primitive: $json")
                    }
                }

                is JsonObject -> {
                    val compound = CompoundBinaryTag.builder()
                    for ((key, value) in json.entrySet()) key?.let { compound.put(it, serialize(value)) }
                    return compound.build()
                }

                is JsonArray -> {
                    val jsonArray = json.asList()
                    if (jsonArray.isEmpty()) return ListBinaryTag.empty()
                    val tagItems: MutableList<BinaryTag> = ArrayList(jsonArray.size)
                    var listType: BinaryTagType<out BinaryTag?>? = null
                    for (jsonEl in jsonArray) {
                        val tag = serialize(jsonEl)
                        tagItems.add(tag)
                        if (listType == null) {
                            listType = tag.type()
                        } else if (listType !== tag.type()) {
                            listType = BinaryTagTypes.COMPOUND
                        }
                    }
                    require(listType != null) { "listType cannot be null!" }
                    when (listType.id().toInt()) {
                        1 -> {
                            val bytes = ByteArray(jsonArray.size)
                            for (i in bytes.indices) bytes[i] = jsonArray[i].asNumber as Byte
                            return bytes.toNbt()
                        }

                        3 -> {
                            val ints = IntArray(jsonArray.size)
                            for (i in ints.indices) ints[i] = jsonArray[i].asNumber as Int
                            return ints.toNbt()
                        }

                        4 -> {
                            val longs = LongArray(jsonArray.size)
                            for (i in jsonArray.indices) longs[i] = jsonArray[i].asNumber as Long
                            return longs.toNbt()
                        }

                        10 -> tagItems.replaceAll { tag: BinaryTag ->
                            if (tag.type() == BinaryTagTypes.COMPOUND) tag else tag.toNamed()
                        }
                    }
                    return ListBinaryTag.listBinaryTag(listType, tagItems)
                }
            }
            return EndBinaryTag.endBinaryTag()
        }
    }


}