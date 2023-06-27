package catmoe.fallencrystal.moefilter.network.bungee.handler

import catmoe.fallencrystal.moefilter.network.bungee.util.ExceptionCatcher.handle
import catmoe.fallencrystal.moefilter.network.bungee.util.exception.PacketOutOfBoundsException
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter

@Sharable
class BungeeHandler : ChannelInboundHandlerAdapter() {
    @Suppress("OVERRIDE_DEPRECATION")
    @Throws(Exception::class)
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) { handle(ctx.channel(), cause) }

    @Throws(Exception::class)
    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        if (msg is ByteBuf) {
            if (!ctx.channel().isActive && msg.refCnt() > 0) { msg.release(msg.refCnt()); return }
            if (!ctx.channel().isActive || !msg.isReadable) { msg.skipBytes(msg.readableBytes()); return }
            msg.markReaderIndex()
            if (msg.readableBytes() > 2048 || msg.capacity() > 4096 || msg.writableBytes() > 4096 || msg.writerIndex() > 1024 || msg.readerIndex() > 2048 || msg.readableBytes() <= 0) {
                msg.clear(); throw PacketOutOfBoundsException("$msg reached packets limit.")
            }

            // the first byte cannot be below 0 as it's the size of the first packet
            // the second byte is the handshake packet id which is always 0
            if (msg.readByte() < 0) throw PacketOutOfBoundsException("First byte cannot be below 0 as it's the size of the first packet")
            msg.resetReaderIndex()
            ctx.fireChannelRead(msg)
            ctx.pipeline().remove(this)
        } else { super.channelRead(ctx, msg) }
    }
}
