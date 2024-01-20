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

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import net.miaomoe.blessing.protocol.exceptions.DecoderException

// https://github.com/CatMoe/Blessing/blob/archived/common/src/main/kotlin/catmoe/fallencrystal/moefilter/network/common/decoder/VarIntFrameDecoder.kt
class VarintFrameDecoder : ByteToMessageDecoder() {
    @Throws(Exception::class)
    override fun decode(
        ctx: ChannelHandlerContext,
        byteBuf: ByteBuf,
        out: MutableList<Any>
    ) {
        if (!ctx.channel().isActive || !byteBuf.isReadable) return
        val reader = VarintByteDecoder()
        val end = byteBuf.forEachByte(reader)
        if (end == -1) {
            if (reader.result === VarintByteDecoder.DecoderResult.RUN_OF_ZEROS) byteBuf.clear()
            return
        }
        when (reader.result) {
            VarintByteDecoder.DecoderResult.RUN_OF_ZEROS -> { byteBuf.readerIndex(end) }
            VarintByteDecoder.DecoderResult.SUCCESS -> {
                val readVarInt = reader.readVarint
                val bytesRead = reader.bytesRead
                if (readVarInt < 0) { byteBuf.clear() } else if (readVarInt == 0) {
                    byteBuf.readerIndex(end + 1)
                } else {
                    val minimumRead = bytesRead + readVarInt
                    if (byteBuf.isReadable(minimumRead)) {
                        out.add(byteBuf.retainedSlice(end + 1, readVarInt))
                        byteBuf.skipBytes(minimumRead)
                    }
                }
            }
            else -> { byteBuf.clear(); throw exception }
        }
    }
    companion object {
        private val exception = DecoderException("Failed to decode varint.")
    }
}