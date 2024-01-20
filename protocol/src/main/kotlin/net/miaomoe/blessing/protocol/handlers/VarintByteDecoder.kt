/*
 * Copyright (C) 2023-2024. CatMoe / Blessing Contributors
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

package net.miaomoe.blessing.protocol.handlers

import io.netty.util.ByteProcessor


// https://github.com/CatMoe/Blessing/blob/archived/common/src/main/kotlin/catmoe/fallencrystal/moefilter/network/common/decoder/VarIntByteDecoder.kt
@Suppress("MemberVisibilityCanBePrivate")
class VarintByteDecoder : ByteProcessor {

    var result = DecoderResult.TOO_SMALL
    var readVarint = 0
    var bytesRead = 0

    override fun process(b: Byte): Boolean {
        if (b.toInt() == 0 && bytesRead == 0) { result = DecoderResult.RUN_OF_ZEROS; return true }
        if (result === DecoderResult.RUN_OF_ZEROS) return false
        readVarint = readVarint or (b.toInt() and 0x7F shl bytesRead++ * 7)
        if (bytesRead > 3) { result = DecoderResult.TOO_BIG; return false }
        if (b.toInt() and 0x80 != 128) { result = DecoderResult.SUCCESS; return false }
        return true
    }

    enum class DecoderResult {
        SUCCESS,
        TOO_SMALL,
        TOO_BIG,
        RUN_OF_ZEROS
    }

}