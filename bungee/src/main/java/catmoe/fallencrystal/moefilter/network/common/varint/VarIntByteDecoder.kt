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

package catmoe.fallencrystal.moefilter.network.common.varint

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
