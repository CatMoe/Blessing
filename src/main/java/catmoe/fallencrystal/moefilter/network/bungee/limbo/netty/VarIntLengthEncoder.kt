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

package catmoe.fallencrystal.moefilter.network.bungee.limbo.netty

import catmoe.fallencrystal.moefilter.network.bungee.limbo.packet.ByteMessage
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
}