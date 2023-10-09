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

package catmoe.fallencrystal.moefilter.network.limbo.netty

import catmoe.fallencrystal.moefilter.network.common.exception.PacketOverflowException
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter

@Suppress("MemberVisibilityCanBePrivate")
class TrafficLimiter(
    var sizeLimit: Int,
    var incomingLimit: Int,
    var byteLimit: Int
) : ChannelInboundHandlerAdapter() {

    var lastTime = System.currentTimeMillis()
    var packetsPerSec = 0
    var bytesPerSec = 0

    @Suppress("GrazieInspection", "ConvertTwoComparisonsToRangeCheck")
    @Throws(Exception::class)
    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        if (msg is ByteBuf) {
            val size = msg.readableBytes()
            //if (this.sizeLimit in 1..<size) throw PacketOverflowException("Readable byte(s) is $size but limit is ${this.sizeLimit}")
            if (sizeLimit > 1 && size > sizeLimit) throw PacketOverflowException("Readable byte(s) is $size but limit is ${this.sizeLimit}")
            val now = System.currentTimeMillis()
            if ((now - lastTime) > 1000) { packetsPerSec=0; bytesPerSec=0; lastTime=now }
            packetsPerSec++; bytesPerSec += size
            //if (incomingLimit in 1..<packetsPerSec) throw PacketOverflowException("Throttled because connection reached $incomingLimit packet(s) per/sec limit")
            //if (byteLimit in 1..<bytesPerSec) throw PacketOverflowException("Throttled because connection reached $byteLimit byte(s) per/sec limit")
            if (incomingLimit > 1 && packetsPerSec > incomingLimit) throw PacketOverflowException("Throttled because connection reached $incomingLimit packet(s) per/sec limit")
            if (byteLimit > 1 && bytesPerSec > byteLimit) throw PacketOverflowException("Throttled because connection reached $byteLimit byte(s) per/sec limit")
        }
        super.channelRead(ctx, msg)
    }

    companion object {
        const val NAME = "moe-traffic-limit"
    }

}