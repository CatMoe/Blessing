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

package catmoe.fallencrystal.moefilter.network.limbo.compat.message

import catmoe.fallencrystal.moefilter.network.common.ByteMessage
import catmoe.fallencrystal.translation.utils.component.ComponentUtil
import catmoe.fallencrystal.translation.utils.version.Version
import com.google.gson.*
import net.kyori.adventure.nbt.*
import net.kyori.adventure.text.Component
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.chat.ComponentSerializer
import java.util.*

data class NbtMessage(var json: String, var tag: BinaryTag) {
    fun write(byteBuf: ByteMessage, version: Version?) =
        if (version?.moreOrEqual(Version.V1_20_3) == true)
            byteBuf.writeNamelessCompoundTag(tag)
        else
            byteBuf.writeString(json)

    companion object {
        fun create(json: String): NbtMessage {
            val compoundBinaryTag = fromJson(JsonParser.parseString(json))
            return NbtMessage(json, compoundBinaryTag)
        }

        fun create(baseComponent: BaseComponent) = this.create(ComponentSerializer.toString(baseComponent))

        fun create(component: Component)  = this.create(ComponentUtil.toGson(component))

        private fun fromJson(json: JsonElement): BinaryTag {
            when (json) {
                is JsonPrimitive -> {
                    return when {
                        json.isNumber -> when (val number = json.getAsNumber()) {
                            is Byte -> ByteBinaryTag.byteBinaryTag(number)
                            is Short -> ShortBinaryTag.shortBinaryTag(number)
                            is Int -> IntBinaryTag.intBinaryTag(number)
                            is Long -> LongBinaryTag.longBinaryTag(number)
                            is Float -> FloatBinaryTag.floatBinaryTag(number)
                            is Double -> DoubleBinaryTag.doubleBinaryTag(number)
                            else -> throw IllegalArgumentException("Unknown number type: $number")
                        }
                        json.isString -> StringBinaryTag.stringBinaryTag(json.asString)
                        json.isBoolean -> ByteBinaryTag.byteBinaryTag(if (json.asBoolean) 1.toByte() else 0.toByte())
                        else -> throw IllegalArgumentException("Unknown JSON primitive: $json")
                    }
                }

                is JsonObject -> {
                    val builder = CompoundBinaryTag.builder()
                    for ((key, value) in json.entrySet()) builder.put(key, fromJson(value))
                    return builder.build()
                }

                is JsonArray -> {
                    val jsonArray: List<JsonElement> = json.asList()
                    if (jsonArray.isEmpty()) {
                        return ListBinaryTag.listBinaryTag(EndBinaryTag.endBinaryTag().type(), Collections.emptyList())
                    }
                    val tagByteType: BinaryTagType<*> = ByteBinaryTag.ZERO.type()
                    val tagIntType: BinaryTagType<*> = IntBinaryTag.intBinaryTag(0).type()
                    val tagLongType: BinaryTagType<*> = LongBinaryTag.longBinaryTag(0).type()
                    val listTag: BinaryTag
                    when (val listType = fromJson(jsonArray[0]).type()) {
                        tagByteType -> {
                            val bytes = ByteArray(jsonArray.size)
                            for (i in bytes.indices) bytes[i] = (jsonArray[i] as JsonPrimitive).asNumber as Byte
                            listTag = ByteArrayBinaryTag.byteArrayBinaryTag(*bytes)
                        }
                        tagIntType -> {
                            val ints = IntArray(jsonArray.size)
                            for (i in ints.indices) ints[i] = (jsonArray[i] as JsonPrimitive).asNumber as Int
                            listTag = IntArrayBinaryTag.intArrayBinaryTag(*ints)
                        }
                        tagLongType -> {
                            val longs = LongArray(jsonArray.size)
                            for (i in longs.indices) longs[i] = (jsonArray[i] as JsonPrimitive).asNumber as Long
                            listTag = LongArrayBinaryTag.longArrayBinaryTag(*longs)
                        }
                        else -> {
                            val tagItems: MutableList<BinaryTag> = ArrayList(jsonArray.size)
                            for (jsonEl in jsonArray) {
                                val subTag = fromJson(jsonEl)
                                require(subTag.type() === listType) { "Cannot convert mixed JsonArray to Tag" }
                                tagItems.add(subTag)
                            }
                            listTag = ListBinaryTag.listBinaryTag(listType, tagItems)
                        }
                    }
                    return listTag
                }

                is JsonNull -> {
                    return EndBinaryTag.endBinaryTag()
                }
            }
            throw IllegalArgumentException("Unknown JSON element: $json")
        }
    }
}