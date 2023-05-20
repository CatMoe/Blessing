package catmoe.fallencrystal.moefilter.network.decoder

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder

class VarIntFrameDecoder : ByteToMessageDecoder() {
    @Throws(Exception::class)
    override fun decode(ctx: ChannelHandlerContext, byteBuf: ByteBuf, out: MutableList<Any>) {
        if (!ctx.channel().isActive || !byteBuf.isReadable) { byteBuf.clear(); return }
        val reader = VarIntByteDecoder()
        val end = byteBuf.forEachByte(reader)
        if (end == -1) { if (reader.getResult() === DecoderResult.EMPTY) { byteBuf.clear() }; return }
        when (reader.getResult()) {
            DecoderResult.EMPTY -> { byteBuf.readerIndex(end) }
            DecoderResult.SUCCESS -> {
                val readVarInt = reader.getReadVarInt()
                val bytesRead = reader.getBytesRead()
                if (readVarInt < 0) { byteBuf.clear() } else if (readVarInt == 0) { byteBuf.readerIndex(end + 1) }
                else { val minRead = bytesRead + readVarInt; if (byteBuf.isReadable(minRead)) { out.add(byteBuf.retainedSlice(end + 1, readVarInt)); byteBuf.skipBytes(minRead) } } }
            else -> { byteBuf.clear() }
        }
    }
}