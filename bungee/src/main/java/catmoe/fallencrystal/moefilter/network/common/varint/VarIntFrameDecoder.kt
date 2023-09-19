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

package catmoe.fallencrystal.moefilter.network.common.varint

import catmoe.fallencrystal.moefilter.network.common.ExceptionCatcher
import catmoe.fallencrystal.moefilter.network.common.decoder.DecoderResult
import catmoe.fallencrystal.moefilter.network.common.decoder.VarIntByteDecoder
import catmoe.fallencrystal.moefilter.network.limbo.packet.exception.InvalidVarIntException
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder

// © velocitypowered.com
class VarIntFrameDecoder : ByteToMessageDecoder() {
    @Throws(Exception::class)
    override fun decode(
        ctx: ChannelHandlerContext,
        byteBuf: ByteBuf,
        out: MutableList<Any>
    ) {
        if (!ctx.channel().isActive || !byteBuf.isReadable) return
        val reader = VarIntByteDecoder()
        val end = byteBuf.forEachByte(reader)
        if (end == -1) {
            if (reader.result === DecoderResult.EMPTY) byteBuf.clear()
            return
        }
        when (reader.result) {
            DecoderResult.EMPTY -> { byteBuf.readerIndex(end) }
            DecoderResult.SUCCESS -> {
                val readVarInt = reader.readVarInt
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
            else -> { byteBuf.clear(); throw InvalidVarIntException("Invalid VarInt to decode. Not or wrong minecraft client?") }
        }
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) { ExceptionCatcher.handle(ctx.channel(), cause) }
}