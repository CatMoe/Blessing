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
import net.miaomoe.blessing.nbt.exception.DecodeTagException
import net.miaomoe.blessing.nbt.exception.EncodeTagException
import org.jetbrains.annotations.ApiStatus.Experimental
import java.io.DataInputStream
import java.io.DataOutput
import java.io.InputStream


object NbtUtil {

    @Suppress("MemberVisibilityCanBePrivate")
    val idMaps = mapOf(
        0 to BinaryTagTypes.END,
        1 to BinaryTagTypes.BYTE,
        2 to BinaryTagTypes.SHORT,
        3 to BinaryTagTypes.INT,
        4 to BinaryTagTypes.LONG,
        5 to BinaryTagTypes.FLOAT,
        6 to BinaryTagTypes.DOUBLE,
        7 to BinaryTagTypes.BYTE_ARRAY,
        8 to BinaryTagTypes.STRING,
        9 to BinaryTagTypes.LIST,
        10 to BinaryTagTypes.COMPOUND,
        11 to BinaryTagTypes.INT_ARRAY,
        12 to BinaryTagTypes.LONG_ARRAY
    )

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

    private fun <T : Number> toJsonArray(iterable: Iterable<T>): JsonArray {
        val list = iterable.map { it }
        val jsonArray = JsonArray(list.size)
        for (element in list) jsonArray.add(JsonPrimitive(element))
        return jsonArray
    }

    private inline fun <reified T : Number> List<JsonElement>.toArray(converter: (JsonElement) -> T)
    = Array(size) { index -> converter.invoke(this[index]) }

    private inline fun <R, reified T : Throwable> sneakyThrows(
        throws: (Throwable) -> T, task: () -> R
    ) : R {
        try {
            return task.invoke()
        } catch (exception: Exception) {
            throw (exception as? T) ?: throws.invoke(exception)
        }
    }

    fun readCompoundTag(stream: InputStream): CompoundBinaryTag
    = sneakyThrows(DecodeTagException::create) { BinaryTagIO.reader().read(stream) }

    fun readNamelessTag(stream: DataInputStream): BinaryTag
    = sneakyThrows(DecodeTagException::create) {
        val id = stream.readByte().toInt()
        val tag = idMaps[id]
        ?: throw DecodeTagException("Unknown nbt for type: $id")
        tag.read(stream)
    }

    fun writeCompoundTag(tag: CompoundBinaryTag, stream: DataOutput)
    = sneakyThrows(EncodeTagException::create) { BinaryTagIO.writer().write(tag, stream) }

    fun writeNamelessTag(tag: BinaryTag, stream: DataOutput) {
        sneakyThrows(EncodeTagException::create) {
            stream.writeByte(tag.type().id().toInt())
            when (tag) {
                is CompoundBinaryTag -> tag.type().write(tag, stream)
                is ByteBinaryTag -> tag.type().write(tag, stream)
                is ShortBinaryTag -> tag.type().write(tag, stream)
                is IntBinaryTag -> tag.type().write(tag, stream)
                is LongBinaryTag -> tag.type().write(tag, stream)
                is DoubleBinaryTag -> tag.type().write(tag, stream)
                is StringBinaryTag -> tag.type().write(tag, stream)
                is ListBinaryTag -> tag.type().write(tag, stream)
                is EndBinaryTag -> tag.type().write(tag, stream)
                else -> throw EncodeTagException("Unknown tag type: $tag")
            }
        }
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
                return when {
                    json.isNumber -> json.asNumber.toNbt()
                    json.isString -> json.asString.toNbt()
                    json.isBoolean -> json.asBoolean.toNbt()
                    else -> throw IllegalArgumentException("Unknown JSON primitive: $json")
                }
            }
            is JsonObject -> {
                val compound = CompoundBinaryTag.builder()
                for ((key, value) in json.entrySet()) compound.put(key, serialize(value))
                return compound.build()
            }
            is JsonArray -> {
                val jsonArray = json.asList()
                if (jsonArray.isEmpty()) return ListBinaryTag.empty()
                val items: MutableList<BinaryTag> = ArrayList(jsonArray.size)
                var listType: BinaryTagType<out BinaryTag>? = null
                for (element in jsonArray) {
                    val tag = serialize(element)
                    items.add(tag)
                    if (listType == null) { listType = tag.type() } else if (listType !== tag.type()) { listType = BinaryTagTypes.COMPOUND }
                }
                when (listType!!.id().toInt()) {
                    1 -> return jsonArray.toArray(JsonElement::getAsByte).toByteArray().toNbt()
                    3 -> return jsonArray.toArray(JsonElement::getAsInt).toIntArray().toNbt()
                    4 -> return jsonArray.toArray(JsonElement::getAsLong).toLongArray().toNbt()
                    10 -> items.replaceAll { tag: BinaryTag -> if (tag.type() === BinaryTagTypes.COMPOUND) tag else tag.toNamed() }
                }
                return ListBinaryTag.listBinaryTag(listType, items)
            }
        }
        return EndBinaryTag.endBinaryTag()
    }
}