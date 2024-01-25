/*
 * Copyright (C) 2023-2024. CatMoe / Blessing Contributors
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

package net.miaomoe.blessing.protocol.util

import io.netty.buffer.*
import io.netty.util.ByteProcessor
import net.kyori.adventure.nbt.*
import net.miaomoe.blessing.nbt.chat.MixedComponent
import net.miaomoe.blessing.protocol.exceptions.DecoderException
import net.miaomoe.blessing.protocol.exceptions.EncodeTagException
import net.miaomoe.blessing.protocol.version.Version
import java.io.Closeable
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import java.nio.channels.GatheringByteChannel
import java.nio.channels.ScatteringByteChannel
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.math.min

@Suppress("DEPRECATION", "MemberVisibilityCanBePrivate", "IdentifierGrammar")
class ByteMessage(private val buf: ByteBuf) : ByteBuf(), Closeable {

    fun toByteArray(): ByteArray {
        val bytes = ByteArray(buf.readableBytes())
        buf.readBytes(bytes)
        return bytes
    }

    fun readVarInt(): Int {
        var i = 0
        val maxRead = min(5.0, buf.readableBytes().toDouble()).toInt()
        for (j in 0 until maxRead) {
            val k = buf.readByte().toInt()
            i = i or (k and 0x7F shl j * 7)
            if (k and 0x80 != 128) {
                return i
            }
        }
        // Throwing
        buf.readBytes(maxRead)
        throw DecoderException("Failed to read Varint.")
    }

    // Translate from https://github.com/PaperMC/Velocity/blob/07a525be7f90f1f3ccd515f7c196824d12ed0fff/proxy/src/main/java/com/velocitypowered/proxy/protocol/ProtocolUtils.java#L130-L163
    fun writeVarInt(value: Int) {
        when {
            (value and -0x80 == 0) -> writeByte(value)
            (value and -0x4000 == 0) -> writeShort(value and 0x7F or 0x80 shl 8 or (value ushr 7 and 0x7F))
            (value and -0x200000 == 0) -> writeMedium(value and 0x7F or 0x80 shl 16 or (value ushr 7 and 0x7F or 0x80 shl 8) or (value ushr 14 and 0x7F))
            (value and -0x10000000 == 0) -> writeInt(value and 0x7F or 0x80 shl 24 or (value ushr 7 and 0x7F or 0x80 shl 16) or (value ushr 14 and 0x7F or 0x80 shl 8) or (value ushr 21 and 0x7F))
            else -> {
                writeInt(value and 0x7F or 0x80 shl 24 or (value ushr 7 and 0x7F or 0x80 shl 16) or (value ushr 14 and 0x7F or 0x80 shl 8) or (value ushr 21 and 0x7F or 0x80))
                writeByte(value ushr 28)
            }
        }
    }

    fun readVarLong(): Long {
        var value = 0L
        var size = 0
        var b: Int
        do {
            b = readByte().toInt()
            value = value or ((b and 0x7F).toLong() shl (size++ * 7))
            require(size <= 10) { "VarLong too long (length must be <= 10)" }
        } while ((b and 0x80) == 0x80)
        return value
    }

    fun writeVarLong(long: Long) = VarLongUtil.writeVarLong(this, long)

    @JvmOverloads
    fun readString(length: Int = readVarInt(), limit: Int = -1): String {
        require(this.isReadable(length)) { "Out of range! Required $length but readable bytes length is ${readableBytes()}." }
        val str = buf.toString(buf.readerIndex(), length, StandardCharsets.UTF_8)
        require(limit == -1 || str.length <= limit) { "String out of range! limit is $limit but received ${str.length}." }
        buf.skipBytes(length)
        return str
    }

    fun writeString(str: CharSequence?) {
        val size = ByteBufUtil.utf8Bytes(str)
        writeVarInt(size)
        buf.writeCharSequence(str, StandardCharsets.UTF_8)
    }

    fun readBytesArray(): ByteArray {
        val length = readVarInt()
        val array = ByteArray(length)
        buf.readBytes(array)
        return array
    }

    fun writeBytesArray(array: ByteArray) {
        writeVarInt(array.size)
        buf.writeBytes(array)
    }

    fun readIntArray(): IntArray {
        val len = readVarInt()
        val array = IntArray(len)
        for (i in 0 until len) { array[i] = readVarInt() }
        return array
    }

    fun readStringsArray(): Array<String?> {
        val length = readVarInt()
        val ret = arrayOfNulls<String>(length)
        for (i in 0 until length) { ret[i] = readString() }
        return ret
    }

    fun writeStringsArray(stringArray: Array<String?>) {
        writeVarInt(stringArray.size)
        stringArray.forEach { writeString(it) }
    }

    fun writeVarIntArray(array: IntArray) {
        writeVarInt(array.size)
        array.forEach { writeVarInt(it) }
    }

    fun writeLongArray(array: LongArray) {
        writeVarInt(array.size)
        array.forEach { writeLong(it) }
    }

    // Legacy methods for 1.7

    fun readUUID() = UUID(readLong(), readLong())

    fun writeUUID(uuid: UUID) {
        writeLong(uuid.mostSignificantBits)
        writeLong(uuid.leastSignificantBits)
    }

    fun writeUUID(uuid: UUID, version: Version) {
        when {
            version.moreOrEqual(Version.V1_19) -> writeUUID(uuid)
            version.moreOrEqual(Version.V1_16) -> writeUUIDIntArray(uuid)
            version.moreOrEqual(Version.V1_7_6) -> writeString(uuid.toString())
            else -> writeString(UUIDUtil.toLegacyFormat(uuid))
        }
    }

    // Ref: https://github.com/jonesdevelopment/sonar/blob/main/sonar-common/src/main/java/xyz/jonesdev/sonar/common/utility/protocol/ProtocolUtil.java
    fun writeUUIDIntArray(uuid: UUID) {
        fun write(value: Long) {
            writeInt((value ushr 32).toInt())
            writeInt(value.toInt())
        }
        write(uuid.mostSignificantBits)
        write(uuid.leastSignificantBits)
    }

    /* NBT */
    fun writeCompoundTagArray(tags: Array<CompoundBinaryTag>) {
        writeVarInt(tags.size)
        for (tag in tags) writeCompoundTag(tag)
    }

    fun writeCompoundTag(tag: CompoundBinaryTag) {
        try {
            ByteBufOutputStream(this).use { BinaryTagIO.writer().write(tag, it as OutputStream) }
        } catch (exception: IOException) {
            throw EncodeTagException(exception)
        }
    }

    fun writeNamelessTag(binaryTag: BinaryTag) {
        try {
            ByteBufOutputStream(this).use {
                it.writeByte(binaryTag.type().id().toInt())
                when (binaryTag) {
                    is CompoundBinaryTag -> binaryTag.type().write(binaryTag, it)
                    is ByteBinaryTag -> binaryTag.type().write(binaryTag, it)
                    is ShortBinaryTag -> binaryTag.type().write(binaryTag, it)
                    is IntBinaryTag -> binaryTag.type().write(binaryTag, it)
                    is LongBinaryTag -> binaryTag.type().write(binaryTag, it)
                    is DoubleBinaryTag -> binaryTag.type().write(binaryTag, it)
                    is StringBinaryTag -> binaryTag.type().write(binaryTag, it)
                    is ListBinaryTag -> binaryTag.type().write(binaryTag, it)
                    is EndBinaryTag -> binaryTag.type().write(binaryTag, it)
                    else -> throw IOException("Unknown tag type: $binaryTag")
                }
            }
        } catch (exception: IOException) {
            throw EncodeTagException(exception)
        }
    }

    fun writeChat(component: MixedComponent, version: Version) {
        if (version.moreOrEqual(Version.V1_19_3))
            writeNamelessTag(component.tag)
        else
            writeString(component.json)
    }

    /* Delegated methods */
    override fun capacity() = buf.capacity()
    override fun capacity(newCapacity: Int): ByteBuf = buf.capacity(newCapacity)
    override fun maxCapacity(): Int = buf.maxCapacity()
    override fun alloc(): ByteBufAllocator = buf.alloc()
    @Suppress("OVERRIDE_DEPRECATION")
    override fun order(): ByteOrder = buf.order()
    @Suppress("OVERRIDE_DEPRECATION")
    override fun order(endianness: ByteOrder): ByteBuf = buf.order(endianness)
    override fun unwrap(): ByteBuf = buf.unwrap()
    override fun isDirect() = buf.isDirect
    override fun isReadOnly() = buf.isReadOnly
    override fun asReadOnly(): ByteBuf = buf.asReadOnly()
    override fun readerIndex() = buf.readerIndex()
    override fun readerIndex(readerIndex: Int): ByteBuf = buf.readerIndex(readerIndex)
    override fun writerIndex() = buf.writerIndex()
    override fun writerIndex(writerIndex: Int): ByteBuf = buf.writerIndex(writerIndex)
    override fun setIndex(readerIndex: Int, writerIndex: Int): ByteBuf = buf.setIndex(readerIndex, writerIndex)
    override fun readableBytes() = buf.readableBytes()
    override fun writableBytes() = buf.writableBytes()
    override fun maxWritableBytes() = buf.maxWritableBytes()
    override fun maxFastWritableBytes() = buf.maxFastWritableBytes()
    override fun isReadable() = buf.isReadable
    override fun isReadable(size: Int) = buf.isReadable(size)
    override fun isWritable() = buf.isWritable
    override fun isWritable(size: Int) = buf.isWritable(size)
    override fun clear(): ByteBuf = buf.clear()
    override fun markReaderIndex(): ByteBuf = buf.markReaderIndex()
    override fun resetReaderIndex(): ByteBuf = buf.resetReaderIndex()
    override fun markWriterIndex(): ByteBuf = buf.markWriterIndex()
    override fun resetWriterIndex(): ByteBuf = buf.resetWriterIndex()
    override fun discardReadBytes(): ByteBuf = buf.discardReadBytes()
    override fun discardSomeReadBytes(): ByteBuf = buf.discardSomeReadBytes()
    override fun ensureWritable(minWritableBytes: Int): ByteBuf = buf.ensureWritable(minWritableBytes)
    override fun ensureWritable(minWritableBytes: Int, force: Boolean) = buf.ensureWritable(minWritableBytes, force)
    override fun getBoolean(index: Int) = buf.getBoolean(index)
    override fun getByte(index: Int) = buf.getByte(index)
    override fun getUnsignedByte(index: Int) = buf.getUnsignedByte(index)
    override fun getShort(index: Int) = buf.getShort(index)
    override fun getShortLE(index: Int) = buf.getShortLE(index)
    override fun getUnsignedShort(index: Int) = buf.getUnsignedShort(index)
    override fun getUnsignedShortLE(index: Int) = buf.getUnsignedShortLE(index)
    override fun getMedium(index: Int) = buf.getMedium(index)
    override fun getMediumLE(index: Int) = buf.getMediumLE(index)
    override fun getUnsignedMedium(index: Int) = buf.getUnsignedMedium(index)
    override fun getUnsignedMediumLE(index: Int) = buf.getUnsignedMediumLE(index)
    override fun getInt(index: Int) = buf.getInt(index)
    override fun getIntLE(index: Int) = buf.getIntLE(index)
    override fun getUnsignedInt(index: Int) = buf.getUnsignedInt(index)
    override fun getUnsignedIntLE(index: Int) = buf.getUnsignedIntLE(index)
    override fun getLong(index: Int) = buf.getLong(index)
    override fun getLongLE(index: Int) = buf.getLongLE(index)
    override fun getChar(index: Int) = buf.getChar(index)
    override fun getFloat(index: Int) = buf.getFloat(index)
    override fun getFloatLE(index: Int) = buf.getFloatLE(index)
    override fun getDouble(index: Int) = buf.getDouble(index)
    override fun getDoubleLE(index: Int) = buf.getDoubleLE(index)
    override fun getBytes(index: Int, dst: ByteBuf): ByteBuf = buf.getBytes(index, dst)
    override fun getBytes(index: Int, dst: ByteBuf, length: Int): ByteBuf = buf.getBytes(index, dst, length)
    override fun getBytes(index: Int, dst: ByteBuf, dstIndex: Int, length: Int): ByteBuf = buf.getBytes(index, dst, dstIndex, length)
    override fun getBytes(index: Int, dst: ByteArray): ByteBuf = buf.getBytes(index, dst)
    override fun getBytes(index: Int, dst: ByteArray, dstIndex: Int, length: Int): ByteBuf = buf.getBytes(index, dst, dstIndex, length)
    override fun getBytes(index: Int, dst: ByteBuffer): ByteBuf = buf.getBytes(index, dst)
    @Throws(IOException::class)
    override fun getBytes(index: Int, out: OutputStream, length: Int): ByteBuf = buf.getBytes(index, out, length)
    @Throws(IOException::class)
    override fun getBytes(index: Int, out: GatheringByteChannel, length: Int): Int = buf.getBytes(index, out, length)
    @Throws(IOException::class)
    override fun getBytes(index: Int, out: FileChannel, position: Long, length: Int): Int = buf.getBytes(index, out, position, length)
    override fun getCharSequence(index: Int, length: Int, charset: Charset): CharSequence = buf.getCharSequence(index, length, charset)
    override fun setBoolean(index: Int, value: Boolean): ByteBuf = buf.setBoolean(index, value)
    override fun setByte(index: Int, value: Int): ByteBuf = buf.setByte(index, value)
    override fun setShort(index: Int, value: Int): ByteBuf = buf.setShort(index, value)
    override fun setShortLE(index: Int, value: Int): ByteBuf = buf.setShortLE(index, value)
    override fun setMedium(index: Int, value: Int): ByteBuf = buf.setMedium(index, value)
    override fun setMediumLE(index: Int, value: Int): ByteBuf = buf.setMediumLE(index, value)
    override fun setInt(index: Int, value: Int): ByteBuf = buf.setInt(index, value)
    override fun setIntLE(index: Int, value: Int): ByteBuf = buf.setIntLE(index, value)
    override fun setLong(index: Int, value: Long): ByteBuf = buf.setLong(index, value)
    override fun setLongLE(index: Int, value: Long): ByteBuf = buf.setLongLE(index, value)
    override fun setChar(index: Int, value: Int): ByteBuf = buf.setChar(index, value)
    override fun setFloat(index: Int, value: Float): ByteBuf = buf.setFloat(index, value)
    override fun setFloatLE(index: Int, value: Float): ByteBuf = buf.setFloatLE(index, value)
    override fun setDouble(index: Int, value: Double): ByteBuf = buf.setDouble(index, value)
    override fun setDoubleLE(index: Int, value: Double): ByteBuf = buf.setDoubleLE(index, value)
    override fun setBytes(index: Int, src: ByteBuf): ByteBuf = buf.setBytes(index, src)
    override fun setBytes(index: Int, src: ByteBuf, length: Int): ByteBuf = buf.setBytes(index, src, length)
    override fun setBytes(index: Int, src: ByteBuf, srcIndex: Int, length: Int): ByteBuf = buf.setBytes(index, src, srcIndex, length)
    override fun setBytes(index: Int, src: ByteArray): ByteBuf = buf.setBytes(index, src)
    override fun setBytes(index: Int, src: ByteArray, srcIndex: Int, length: Int): ByteBuf = buf.setBytes(index, src, srcIndex, length)
    override fun setBytes(index: Int, src: ByteBuffer): ByteBuf = buf.setBytes(index, src)
    @Throws(IOException::class)
    override fun setBytes(index: Int, `in`: InputStream, length: Int): Int = buf.setBytes(index, `in`, length)
    @Throws(IOException::class)
    override fun setBytes(index: Int, `in`: ScatteringByteChannel, length: Int): Int = buf.setBytes(index, `in`, length)
    @Throws(IOException::class)
    override fun setBytes(index: Int, `in`: FileChannel, position: Long, length: Int): Int = buf.setBytes(index, `in`, position, length)
    override fun setZero(index: Int, length: Int): ByteBuf = buf.setZero(index, length)
    override fun setCharSequence(index: Int, sequence: CharSequence, charset: Charset): Int = buf.setCharSequence(index, sequence, charset)
    override fun readBoolean(): Boolean = buf.readBoolean()
    override fun readByte(): Byte = buf.readByte()
    override fun readUnsignedByte(): Short = buf.readUnsignedByte()
    override fun readShort(): Short = buf.readShort()
    override fun readShortLE(): Short = buf.readShortLE()
    override fun readUnsignedShort(): Int = buf.readUnsignedShort()
    override fun readUnsignedShortLE(): Int = buf.readUnsignedShortLE()
    override fun readMedium(): Int = buf.readMedium()
    override fun readMediumLE(): Int = buf.readMediumLE()
    override fun readUnsignedMedium(): Int = buf.readUnsignedMedium()
    override fun readUnsignedMediumLE(): Int = buf.readUnsignedMediumLE()
    override fun readInt(): Int = buf.readInt()
    override fun readIntLE(): Int = buf.readIntLE()
    override fun readUnsignedInt(): Long = buf.readUnsignedInt()
    override fun readUnsignedIntLE(): Long = buf.readUnsignedIntLE()
    override fun readLong(): Long = buf.readLong()
    override fun readLongLE(): Long = buf.readLongLE()
    override fun readChar(): Char = buf.readChar()
    override fun readFloat(): Float = buf.readFloat()
    override fun readFloatLE(): Float = buf.readFloatLE()
    override fun readDouble(): Double = buf.readDouble()
    override fun readDoubleLE(): Double = buf.readDoubleLE()
    override fun readBytes(length: Int): ByteBuf = buf.readBytes(length)
    override fun readSlice(length: Int): ByteBuf = buf.readSlice(length)
    override fun readRetainedSlice(length: Int): ByteBuf = buf.readRetainedSlice(length)
    override fun readBytes(dst: ByteBuf): ByteBuf = buf.readBytes(dst)
    override fun readBytes(dst: ByteBuf, length: Int): ByteBuf = buf.readBytes(dst, length)
    override fun readBytes(dst: ByteBuf, dstIndex: Int, length: Int): ByteBuf = buf.readBytes(dst, dstIndex, length)
    override fun readBytes(dst: ByteArray): ByteBuf = buf.readBytes(dst)
    override fun readBytes(dst: ByteArray, dstIndex: Int, length: Int): ByteBuf = buf.readBytes(dst, dstIndex, length)
    override fun readBytes(dst: ByteBuffer): ByteBuf = buf.readBytes(dst)
    @Throws(IOException::class)
    override fun readBytes(out: OutputStream, length: Int): ByteBuf = buf.readBytes(out, length)
    @Throws(IOException::class)
    override fun readBytes(out: GatheringByteChannel, length: Int): Int = buf.readBytes(out, length)
    override fun readCharSequence(length: Int, charset: Charset): CharSequence = buf.readCharSequence(length, charset)
    @Throws(IOException::class)
    override fun readBytes(out: FileChannel, position: Long, length: Int) = buf.readBytes(out, position, length)
    override fun skipBytes(length: Int): ByteBuf = buf.skipBytes(length)
    override fun writeBoolean(value: Boolean): ByteBuf = buf.writeBoolean(value)
    override fun writeByte(value: Int): ByteBuf = buf.writeByte(value)
    override fun writeShort(value: Int): ByteBuf = buf.writeShort(value)
    override fun writeShortLE(value: Int): ByteBuf = buf.writeShortLE(value)
    override fun writeMedium(value: Int): ByteBuf = buf.writeMedium(value)
    override fun writeMediumLE(value: Int): ByteBuf = buf.writeMediumLE(value)
    override fun writeInt(value: Int): ByteBuf = buf.writeInt(value)
    override fun writeIntLE(value: Int): ByteBuf = buf.writeIntLE(value)
    override fun writeLong(value: Long): ByteBuf = buf.writeLong(value)
    override fun writeLongLE(value: Long): ByteBuf = buf.writeLongLE(value)
    override fun writeChar(value: Int): ByteBuf = buf.writeChar(value)
    override fun writeFloat(value: Float): ByteBuf = buf.writeFloat(value)
    override fun writeFloatLE(value: Float): ByteBuf = buf.writeFloatLE(value)
    override fun writeDouble(value: Double): ByteBuf = buf.writeDouble(value)
    override fun writeDoubleLE(value: Double): ByteBuf = buf.writeDoubleLE(value)
    override fun writeBytes(src: ByteBuf): ByteBuf = buf.writeBytes(src)
    override fun writeBytes(src: ByteBuf, length: Int): ByteBuf = buf.writeBytes(src, length)
    override fun writeBytes(src: ByteBuf, srcIndex: Int, length: Int): ByteBuf = buf.writeBytes(src, srcIndex, length)
    override fun writeBytes(src: ByteArray): ByteBuf = buf.writeBytes(src)
    override fun writeBytes(src: ByteArray, srcIndex: Int, length: Int): ByteBuf = buf.writeBytes(src, srcIndex, length)
    override fun writeBytes(src: ByteBuffer): ByteBuf = buf.writeBytes(src)
    @Throws(IOException::class)
    override fun writeBytes(`in`: InputStream, length: Int) = buf.writeBytes(`in`, length)
    @Throws(IOException::class)
    override fun writeBytes(`in`: ScatteringByteChannel, length: Int) = buf.writeBytes(`in`, length)
    @Throws(IOException::class)
    override fun writeBytes(`in`: FileChannel, position: Long, length: Int) = buf.writeBytes(`in`, position, length)
    override fun writeZero(length: Int): ByteBuf = buf.writeZero(length)
    override fun writeCharSequence(sequence: CharSequence, charset: Charset) = buf.writeCharSequence(sequence, charset)
    override fun indexOf(fromIndex: Int, toIndex: Int, value: Byte) = buf.indexOf(fromIndex, toIndex, value)
    override fun bytesBefore(value: Byte) = buf.bytesBefore(value)
    override fun bytesBefore(length: Int, value: Byte) = buf.bytesBefore(length, value)
    override fun bytesBefore(index: Int, length: Int, value: Byte) = buf.bytesBefore(index, length, value)
    override fun forEachByte(processor: ByteProcessor) = buf.forEachByte(processor)
    override fun forEachByte(index: Int, length: Int, processor: ByteProcessor) = buf.forEachByte(index, length, processor)
    override fun forEachByteDesc(processor: ByteProcessor) = buf.forEachByteDesc(processor)
    override fun forEachByteDesc(index: Int, length: Int, processor: ByteProcessor) = buf.forEachByteDesc(index, length, processor)
    override fun copy(): ByteBuf = buf.copy()
    override fun copy(index: Int, length: Int): ByteBuf = buf.copy(index, length)
    override fun slice(): ByteBuf = buf.slice()
    override fun retainedSlice(): ByteBuf = buf.retainedSlice()
    override fun slice(index: Int, length: Int): ByteBuf = buf.slice(index, length)
    override fun retainedSlice(index: Int, length: Int): ByteBuf = buf.retainedSlice(index, length)
    override fun duplicate(): ByteBuf = buf.duplicate()
    override fun retainedDuplicate(): ByteBuf = buf.retainedDuplicate()
    override fun nioBufferCount() = buf.nioBufferCount()
    override fun nioBuffer(): ByteBuffer = buf.nioBuffer()
    override fun nioBuffer(index: Int, length: Int): ByteBuffer = buf.nioBuffer(index, length)
    override fun internalNioBuffer(index: Int, length: Int): ByteBuffer = buf.internalNioBuffer(index, length)
    override fun nioBuffers(): Array<ByteBuffer> = buf.nioBuffers()
    override fun nioBuffers(index: Int, length: Int): Array<ByteBuffer> = buf.nioBuffers(index, length)
    override fun hasArray() = buf.hasArray()
    override fun array(): ByteArray = buf.array()
    override fun arrayOffset() = buf.arrayOffset()
    override fun hasMemoryAddress() = buf.hasMemoryAddress()
    override fun memoryAddress() = buf.memoryAddress()
    override fun isContiguous() = buf.isContiguous
    override fun toString(charset: Charset): String = buf.toString(charset)
    override fun toString(index: Int, length: Int, charset: Charset): String = buf.toString(index, length, charset)
    override fun hashCode() = buf.hashCode()
    override fun equals(other: Any?) = buf == other
    override fun compareTo(other: ByteBuf) = buf.compareTo(other)
    override fun toString() = buf.toString()
    override fun retain(increment: Int): ByteBuf = buf.retain(increment)
    override fun retain(): ByteBuf = buf.retain()
    override fun touch(): ByteBuf = buf.touch()
    override fun touch(hint: Any): ByteBuf = buf.touch(hint)
    override fun refCnt() = buf.refCnt()
    override fun release() = buf.release()
    override fun release(decrement: Int) = buf.release(decrement)

    override fun close() {
        this.release()
    }

    companion object {
        fun create() = ByteMessage(Unpooled.buffer())
        fun ByteBuf.toByteMessage() = ByteMessage(this)
    }
}