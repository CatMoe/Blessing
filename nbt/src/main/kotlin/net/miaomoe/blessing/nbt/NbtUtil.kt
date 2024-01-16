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

import com.google.gson.internal.LazilyParsedNumber
import net.kyori.adventure.nbt.*

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

    fun List<String>.toListTag() = ListBinaryTag.listBinaryTag(BinaryTagTypes.STRING, this.map { it.toNbt() })

    fun BinaryTag.singleWithCompound(name: String) = CompoundBinaryTag.builder().put(name, this).build()
}