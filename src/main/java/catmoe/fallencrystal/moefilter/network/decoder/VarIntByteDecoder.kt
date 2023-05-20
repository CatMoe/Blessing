package catmoe.fallencrystal.moefilter.network.decoder

import io.netty.util.ByteProcessor

class VarIntByteDecoder : ByteProcessor {
    private var result = DecoderResult.INVALID
    private var readVarInt = 0
    private var bytesRead = 0
    @Throws(Exception::class)
    override fun process(b: Byte): Boolean {
        if (b.toInt() == 0 && bytesRead == 0) { result = DecoderResult.EMPTY; return true }
        if (result === DecoderResult.EMPTY) { return false }
        readVarInt = readVarInt or (b.toInt() and 0x7F shl bytesRead++ * 7)
        if (bytesRead > 3) { result = DecoderResult.INVALID; return false }
        if (b.toInt() and 0x80 != 128) { result = DecoderResult.SUCCESS; return false }
        return true
    }

    fun getResult(): DecoderResult { return result }
    fun getReadVarInt(): Int { return readVarInt }
    fun getBytesRead(): Int { return bytesRead }
}
