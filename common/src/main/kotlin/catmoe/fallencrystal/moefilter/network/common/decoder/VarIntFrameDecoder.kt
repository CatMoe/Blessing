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

package catmoe.fallencrystal.moefilter.network.common.decoder

import catmoe.fallencrystal.moefilter.network.common.exception.InvalidVarIntException
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

    /* Override with MoeFilterBungee AbstractInitializer
    @Suppress("OVERRIDE_DEPRECATION")
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) { ExceptionCatcher.handle(ctx.channel(), cause) }
     */
}