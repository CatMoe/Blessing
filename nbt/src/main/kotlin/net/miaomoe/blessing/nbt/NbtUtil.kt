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

package net.miaomoe.blessing.nbt

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.google.gson.internal.LazilyParsedNumber
import net.kyori.adventure.nbt.*
import org.jetbrains.annotations.ApiStatus.Experimental

object NbtUtil {

    fun BinaryTag.toNamed(name: String = "") = CompoundBinaryTag.builder().put(name, this).build()
    fun String.toNbt() = StringBinaryTag.stringBinaryTag(this)
    fun Byte.toNbt() = ByteBinaryTag.byteBinaryTag(this)
    fun Short.toNbt() = ShortBinaryTag.shortBinaryTag(this)
    fun Int.toNbt() = IntBinaryTag.intBinaryTag(this)
    fun Long.toNbt() = LongBinaryTag.longBinaryTag(this)
    fun Float.toNbt() = FloatBinaryTag.floatBinaryTag(this)
    fun Double.toNbt() = DoubleBinaryTag.doubleBinaryTag(this)
    fun Boolean.toNbt() = (if (this) 1 else 0).toByte().toNbt()
    fun ByteArray.toNbt() = ByteArrayBinaryTag.byteArrayBinaryTag(*this)
    fun IntArray.toNbt() = IntArrayBinaryTag.intArrayBinaryTag(*this)
    fun LongArray.toNbt() = LongArrayBinaryTag.longArrayBinaryTag(*this)

    fun Number.toNbt(): BinaryTag {
        return when (this) {
            is Byte -> this.toNbt()
            is Short -> this.toNbt()
            is Int -> this.toNbt()
            is Long -> this.toNbt()
            is Float -> this.toNbt()
            is Double -> this.toNbt()
            is LazilyParsedNumber -> this.toInt().toNbt()
            else -> throw IllegalArgumentException("Unknown number type: $this")
        }
    }

    fun CompoundBinaryTag.Builder.put(key: String, float: Float) = this.put(key, float.toNbt())
    fun CompoundBinaryTag.Builder.put(key: String, int: Int) = this.put(key, int.toNbt())
    fun CompoundBinaryTag.Builder.put(key: String, string: String) = this.put(key, string.toNbt())
    fun CompoundBinaryTag.Builder.put(key: String, double: Double) = this.put(key, double.toNbt())
    fun CompoundBinaryTag.Builder.put(key: String, boolean: Boolean) = this.put(key, boolean.toNbt())

    fun CompoundBinaryTag.toListTag() = listOf(this).toListTag()

    fun List<CompoundBinaryTag>.toListTag() = ListBinaryTag.listBinaryTag(BinaryTagTypes.COMPOUND, this)

    private fun <T : Number>toJsonArray(iterable: Iterable<T>): JsonArray {
        val list = iterable.map { it }
        val jsonArray = JsonArray(list.size)
        for (element in list) jsonArray.add(JsonPrimitive(element))
        return jsonArray
    }

    @Experimental
    fun deserialize(nbt: BinaryTag): JsonElement = when (nbt) {
        is ByteBinaryTag -> JsonPrimitive(nbt.byteValue())
        is ShortBinaryTag -> JsonPrimitive(nbt.shortValue())
        is IntBinaryTag -> JsonPrimitive(nbt.intValue())
        is LongBinaryTag -> JsonPrimitive(nbt.longValue())
        is DoubleBinaryTag -> JsonPrimitive(nbt.doubleValue())
        is ByteArrayBinaryTag -> toJsonArray(nbt)
        is IntArrayBinaryTag -> toJsonArray(nbt)
        is LongArrayBinaryTag -> toJsonArray(nbt)
        is StringBinaryTag -> JsonPrimitive(nbt.value())
        is ListBinaryTag -> {
            val list = JsonArray(nbt.size())
            for (tag in nbt) {
                if (tag is CompoundBinaryTag && tag.size() == 1)
                    list.add(deserialize(tag[""] ?: continue))
                else
                    list.add(deserialize(tag))
            }
            list
        }
        is CompoundBinaryTag -> {
            val jsonObject = JsonObject()
            for ((name, tag) in nbt) jsonObject.add(name, deserialize(tag))
            jsonObject
        }
        else -> throw IllegalArgumentException("Unknown nbt type: $nbt")
    }

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