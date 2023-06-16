package catmoe.fallencrystal.moefilter.network.bungee.decoder

import io.netty.util.ByteProcessor

class VarIntByteDecoder : ByteProcessor {
    var result = DecoderResult.INVALID
    var readVarInt = 0
    var bytesRead = 0
    @Throws(Exception::class)
    override fun process(b: Byte): Boolean {
        if (b.toInt() == 0 && bytesRead == 0) { result = DecoderResult.EMPTY; return true }
        if (result === DecoderResult.EMPTY) return false
        readVarInt = readVarInt or (b.toInt() and 0x7F shl bytesRead++ * 7)
        if (bytesRead > 3) { result = DecoderResult.INVALID; return false }
        if (b.toInt() and 0x80 != 128) { result = DecoderResult.SUCCESS; return false }
        return true
    }
}
