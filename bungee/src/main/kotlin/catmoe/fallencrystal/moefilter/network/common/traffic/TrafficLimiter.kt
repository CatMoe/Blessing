/*
 * Copyright (C) 2023-2023. CatMoe / MoeFilter Contributors
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

package catmoe.fallencrystal.moefilter.network.common.traffic

import catmoe.fallencrystal.moefilter.network.common.exception.PacketOverflowException
import catmoe.fallencrystal.translation.logger.CubeLogger
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import java.net.InetSocketAddress
import java.util.logging.Level

@Suppress("MemberVisibilityCanBePrivate", "CanBeParameter", "ConvertTwoComparisonsToRangeCheck")
class TrafficLimiter(
    val sizeLimit: Int,
    val interval: Double,
    val maxPacketRate: Double,
    val silentException: Boolean
) : ChannelInboundHandlerAdapter() {

    val packetBucket: PacketBucket? = if (interval > 0.0 && maxPacketRate > 0.0) PacketBucket(interval * 1000.0, 150) else null

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        if (msg is ByteBuf) {
            val bytes = msg.readableBytes()
            if (sizeLimit > 0 && bytes > sizeLimit) throw PacketOverflowException("Packet to large! ($bytes bytes, reached $sizeLimit limit)")
            if (packetBucket != null) {
                packetBucket.incrementPackets(1)
                if (packetBucket.getCurrentPacketRate() > maxPacketRate) throw PacketOverflowException("Too many packets!")
            }
        }
        super.channelRead(ctx, msg)
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        if (silentException) {
            val channel = ctx.channel()
            val c = cause.cause ?: cause
            // Only handle PacketOverflowException to prevent unexpected throwable caught by this.
            if (c is PacketOverflowException) {
                CubeLogger.log(Level.INFO, "[PacketLimiter] ${channel.remoteAddress() as? InetSocketAddress}'s connection is closing: ${c.message}")
                channel.close(); return
            }
        }
        super.exceptionCaught(ctx, cause)
    }

    /*
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
     */


    companion object {
        const val NAME = "moe-traffic-limit"
    }

}