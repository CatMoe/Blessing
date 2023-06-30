package catmoe.fallencrystal.moefilter.network.bungee.decoder

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
                if (readVarInt < 0) { byteBuf.clear() } else if (readVarInt == 0) { byteBuf.readerIndex(end + 1)
                } else {
                    val minimumRead = bytesRead + readVarInt
                    if (byteBuf.isReadable(minimumRead)) {
                        out.add(byteBuf.retainedSlice(end + 1, readVarInt))
                        byteBuf.skipBytes(minimumRead)
                    }
                }
            }
            else -> { byteBuf.clear() }
        }
    }

    companion object
}