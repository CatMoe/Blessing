package catmoe.fallencrystal.moefilter.network.handler

import catmoe.fallencrystal.moefilter.network.util.ChannelExceptionCatcher.handle
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter

@Sharable
class BungeeHandler : ChannelInboundHandlerAdapter() {
    @Deprecated("Deprecated in Java", ReplaceWith(
        "handle(ctx.channel(), cause)",
        "catmoe.fallencrystal.moefilter.network.util.ChannelExceptionCatcher.handle"))
    @Throws(Exception::class)
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        handle(ctx.channel(), cause)
    }

    @Throws(Exception::class)
    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        if (msg is ByteBuf) {
            if (!ctx.channel().isActive && msg.refCnt() > 0) { msg.release(msg.refCnt()); return }
            if (!ctx.channel().isActive || !msg.isReadable) { msg.skipBytes(msg.readableBytes());return }
            msg.markReaderIndex()
            // TODO("PacketLimiter here")
            val firstByte = msg.readByte()
            val secondByte = msg.readByte()
            if (firstByte < 0 || secondByte.toInt() != 0) return  // Handshake packet id is 0
            msg.resetReaderIndex()
            ctx.fireChannelRead(msg)
            ctx.pipeline().remove(this)
        } else { super.channelRead(ctx, msg) }
    }
}
