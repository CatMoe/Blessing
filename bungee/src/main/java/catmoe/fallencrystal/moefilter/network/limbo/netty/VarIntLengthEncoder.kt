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

package catmoe.fallencrystal.moefilter.network.limbo.netty

import catmoe.fallencrystal.moefilter.network.common.ExceptionCatcher
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder

class VarIntLengthEncoder : MessageToByteEncoder<ByteBuf>() {
    override fun encode(ctx: ChannelHandlerContext, buf: ByteBuf, out: ByteBuf) {
        val msg = ByteMessage(out)
        msg.writeVarInt(buf.readableBytes())
        msg.writeBytes(buf)
    }

    override fun allocateBuffer(ctx: ChannelHandlerContext, msg: ByteBuf, preferDirect: Boolean): ByteBuf? {
        val anticipatedRequiredCapacity = 5 + msg.readableBytes()
        return ctx.alloc().heapBuffer(anticipatedRequiredCapacity)
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) { ExceptionCatcher.handle(ctx.channel(), cause) }
}