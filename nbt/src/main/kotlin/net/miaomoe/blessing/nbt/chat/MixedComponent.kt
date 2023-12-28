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
import com.google.gson.internal.LazilyParsedNumber
import net.kyori.adventure.nbt.*

class MixedComponent(
    val json: String,
    val tag: BinaryTag
) {

    constructor(json: String) : this(json, serialize(JsonParser.parseString(json)))

    companion object {
        private fun serialize(json: JsonElement): BinaryTag {
            when (json) {
                is JsonPrimitive -> {
                    when {
                        json.isNumber -> return when (val number = json.getAsNumber()) {
                            is Byte -> ByteBinaryTag.byteBinaryTag(number)
                            is Short -> ShortBinaryTag.shortBinaryTag(number)
                            is Int -> IntBinaryTag.intBinaryTag(number)
                            is Long -> LongBinaryTag.longBinaryTag(number)
                            is Float -> FloatBinaryTag.floatBinaryTag(number)
                            is Double -> DoubleBinaryTag.doubleBinaryTag(number)
                            is LazilyParsedNumber -> IntBinaryTag.intBinaryTag(number.toInt())
                            else -> throw IllegalArgumentException("Unknown number type: $number")
                        }
                        json.isString -> return StringBinaryTag.stringBinaryTag(json.asString)
                        json.isBoolean -> ByteBinaryTag.byteBinaryTag((if (json.asBoolean) 1 else 0).toByte())
                        else -> throw IllegalArgumentException("Unknown JSON primitive: $json")
                    }
                }

                is JsonObject -> {
                    val compound = CompoundBinaryTag.builder()
                    for ((key, value) in json.entrySet()) {
                        compound.put(key!!, serialize(value))
                    }
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
                            return ByteArrayBinaryTag.byteArrayBinaryTag(*bytes)
                        }

                        3 -> {
                            val ints = IntArray(jsonArray.size)
                            for (i in ints.indices) ints[i] = jsonArray[i].asNumber as Int
                            return IntArrayBinaryTag.intArrayBinaryTag(*ints)
                        }

                        4 -> {
                            val longs = LongArray(jsonArray.size)
                            for (i in jsonArray.indices) longs[i] = jsonArray[i].asNumber as Long
                            return LongArrayBinaryTag.longArrayBinaryTag(*longs)
                        }

                        10 -> tagItems.replaceAll { tag: BinaryTag ->
                            if (tag.type() == BinaryTagTypes.COMPOUND) tag else CompoundBinaryTag.builder().put("", tag).build()
                        }
                    }
                    return ListBinaryTag.listBinaryTag(listType, tagItems)
                }
            }
            return EndBinaryTag.endBinaryTag()
        }
    }


}