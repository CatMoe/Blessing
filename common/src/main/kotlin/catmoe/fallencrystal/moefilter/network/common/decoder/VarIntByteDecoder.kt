/*
 * Copyright (C) 2020-2023 Velocity Contributors
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

package catmoe.fallencrystal.moefilter.network.common.decoder

import io.netty.util.ByteProcessor

// © velocitypowered.com
class VarIntByteDecoder : ByteProcessor {
    var result = DecoderResult.INVALID
    var readVarInt = 0
    var bytesRead = 0
    @Throws(Exception::class)
    override fun process(b: Byte): Boolean {
        if (b.toInt() == 0 && bytesRead == 0) { result = DecoderResult.EMPTY; return true }
        if (result === DecoderResult.EMPTY) return false
        readVarInt = readVarInt or (b.toInt() and 0x7F shl bytesRead++ * 7)
        if (bytesRead > 3) { result = DecoderResult.INVALID; return false }
        if (b.toInt() and 0x80 != 128) { result = DecoderResult.SUCCESS; return false }
        return true
    }
}
