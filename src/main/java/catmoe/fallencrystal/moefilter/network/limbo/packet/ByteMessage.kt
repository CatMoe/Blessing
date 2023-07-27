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
package catmoe.fallencrystal.moefilter.network.limbo.packet

import catmoe.fallencrystal.moefilter.network.limbo.packet.exception.BitSetTooLargeException
import catmoe.fallencrystal.moefilter.network.limbo.packet.exception.InvalidVarIntException
import io.netty.buffer.*
import io.netty.handler.codec.DecoderException
import io.netty.handler.codec.EncoderException
import io.netty.util.ByteProcessor
import net.kyori.adventure.nbt.BinaryTagIO
import net.kyori.adventure.nbt.CompoundBinaryTag
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

@Suppress("unused", "IdentifierGrammar", "SpellCheckingInspection", "DEPRECATION")
class ByteMessage(private val buf: ByteBuf) : ByteBuf() {
    fun toByteArray(): ByteArray {
        val bytes = ByteArray(buf.readableBytes())
        buf.readBytes(bytes)
        return bytes
    }

    /* Minecraft's protocol methods */
    fun readVarInt(): Int {
        var i = 0
        val maxRead = 5.coerceAtMost(buf.readableBytes())
        for (j in 0 until maxRead) {
            val k = buf.readByte().toInt()
            i = i or (k and 0x7F shl j * 7)
            if (k and 0x80 != 128) { return i }
        }
        buf.readBytes(maxRead)
        throw InvalidVarIntException("Error when reading VarInt: May out of bounds or readByte() is invalid.")
    }

    fun writeVarInt(value: Int) {
        var v = value
        while (true) {
            if (v and -0x80 == 0) { buf.writeByte(v); return }
            buf.writeByte(value and 0x7F or 0x80)
            v = v ushr 7
        }
    }

    @JvmOverloads
    fun readString(length: Int = readVarInt()): String {
        val str = buf.toString(buf.readerIndex(), length, StandardCharsets.UTF_8)
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

    fun readUuid(): UUID {
        val msb = buf.readLong()
        val lsb = buf.readLong()
        return UUID(msb, lsb)
    }

    fun writeUuid(uuid: UUID) {
        buf.writeLong(uuid.mostSignificantBits)
        buf.writeLong(uuid.leastSignificantBits)
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

    fun writeCompoundTagArray(compoundTags: Array<CompoundBinaryTag?>) {
        try {
            ByteBufOutputStream(buf).use { stream ->
                writeVarInt(compoundTags.size)
                compoundTags.forEach { tag -> BinaryTagIO.writer().write(tag!!, (stream as OutputStream)) }
            }
        } catch (e: IOException) {
            throw EncoderException("Cannot write NBT CompoundTag")
        }
    }

    fun readCompoundTag(): CompoundBinaryTag {
        try {
            ByteBufInputStream(buf).use { stream -> return BinaryTagIO.reader().read((stream as InputStream)) }
        } catch (thrown: IOException) {
            throw DecoderException("Cannot read NBT CompoundTag")
        }
    }

    fun writeCompoundTag(compoundTag: CompoundBinaryTag?) {
        try {
            ByteBufOutputStream(buf).use { stream ->
                BinaryTagIO.writer().write(compoundTag!!, (stream as OutputStream))
            }
        } catch (e: IOException) {
            throw EncoderException("Cannot write NBT CompoundTag")
        }
    }

    fun <E : Enum<E>?> writeEnumSet(enumset: EnumSet<E>, oclass: Class<E>) {
        val enums = oclass.enumConstants
        val bits = BitSet(enums.size)
        for (i in enums.indices) {
            bits[i] = enumset.contains(enums[i])
        }
        writeFixedBitSet(
            bits,
            enums.size,
            buf
        )
    }

    /* Delegated methods */
    override fun capacity(): Int {
        return buf.capacity()
    }

    override fun capacity(newCapacity: Int): ByteBuf {
        return buf.capacity(newCapacity)
    }

    override fun maxCapacity(): Int {
        return buf.maxCapacity()
    }

    override fun alloc(): ByteBufAllocator {
        return buf.alloc()
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun order(): ByteOrder {
        return buf.order()
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun order(endianness: ByteOrder): ByteBuf {
        return buf.order(endianness)
    }

    override fun unwrap(): ByteBuf {
        return buf.unwrap()
    }

    override fun isDirect(): Boolean {
        return buf.isDirect
    }

    override fun isReadOnly(): Boolean {
        return buf.isReadOnly
    }

    override fun asReadOnly(): ByteBuf {
        return buf.asReadOnly()
    }

    override fun readerIndex(): Int {
        return buf.readerIndex()
    }

    override fun readerIndex(readerIndex: Int): ByteBuf {
        return buf.readerIndex(readerIndex)
    }

    override fun writerIndex(): Int {
        return buf.writerIndex()
    }

    override fun writerIndex(writerIndex: Int): ByteBuf {
        return buf.writerIndex(writerIndex)
    }

    override fun setIndex(readerIndex: Int, writerIndex: Int): ByteBuf {
        return buf.setIndex(readerIndex, writerIndex)
    }

    override fun readableBytes(): Int {
        return buf.readableBytes()
    }

    override fun writableBytes(): Int {
        return buf.writableBytes()
    }

    override fun maxWritableBytes(): Int {
        return buf.maxWritableBytes()
    }

    override fun maxFastWritableBytes(): Int {
        return buf.maxFastWritableBytes()
    }

    override fun isReadable(): Boolean {
        return buf.isReadable
    }

    override fun isReadable(size: Int): Boolean {
        return buf.isReadable(size)
    }

    override fun isWritable(): Boolean {
        return buf.isWritable
    }

    override fun isWritable(size: Int): Boolean {
        return buf.isWritable(size)
    }

    override fun clear(): ByteBuf {
        return buf.clear()
    }

    override fun markReaderIndex(): ByteBuf {
        return buf.markReaderIndex()
    }

    override fun resetReaderIndex(): ByteBuf {
        return buf.resetReaderIndex()
    }

    override fun markWriterIndex(): ByteBuf {
        return buf.markWriterIndex()
    }

    override fun resetWriterIndex(): ByteBuf {
        return buf.resetWriterIndex()
    }

    override fun discardReadBytes(): ByteBuf {
        return buf.discardReadBytes()
    }

    override fun discardSomeReadBytes(): ByteBuf {
        return buf.discardSomeReadBytes()
    }

    override fun ensureWritable(minWritableBytes: Int): ByteBuf {
        return buf.ensureWritable(minWritableBytes)
    }

    override fun ensureWritable(minWritableBytes: Int, force: Boolean): Int {
        return buf.ensureWritable(minWritableBytes, force)
    }

    override fun getBoolean(index: Int): Boolean {
        return buf.getBoolean(index)
    }

    override fun getByte(index: Int): Byte {
        return buf.getByte(index)
    }

    override fun getUnsignedByte(index: Int): Short {
        return buf.getUnsignedByte(index)
    }

    override fun getShort(index: Int): Short {
        return buf.getShort(index)
    }

    override fun getShortLE(index: Int): Short {
        return buf.getShortLE(index)
    }

    override fun getUnsignedShort(index: Int): Int {
        return buf.getUnsignedShort(index)
    }

    override fun getUnsignedShortLE(index: Int): Int {
        return buf.getUnsignedShortLE(index)
    }

    override fun getMedium(index: Int): Int {
        return buf.getMedium(index)
    }

    override fun getMediumLE(index: Int): Int {
        return buf.getMediumLE(index)
    }

    override fun getUnsignedMedium(index: Int): Int {
        return buf.getUnsignedMedium(index)
    }

    override fun getUnsignedMediumLE(index: Int): Int {
        return buf.getUnsignedMediumLE(index)
    }

    override fun getInt(index: Int): Int {
        return buf.getInt(index)
    }

    override fun getIntLE(index: Int): Int {
        return buf.getIntLE(index)
    }

    override fun getUnsignedInt(index: Int): Long {
        return buf.getUnsignedInt(index)
    }

    override fun getUnsignedIntLE(index: Int): Long {
        return buf.getUnsignedIntLE(index)
    }

    override fun getLong(index: Int): Long {
        return buf.getLong(index)
    }

    override fun getLongLE(index: Int): Long {
        return buf.getLongLE(index)
    }

    override fun getChar(index: Int): Char {
        return buf.getChar(index)
    }

    override fun getFloat(index: Int): Float {
        return buf.getFloat(index)
    }

    override fun getFloatLE(index: Int): Float {
        return buf.getFloatLE(index)
    }

    override fun getDouble(index: Int): Double {
        return buf.getDouble(index)
    }

    override fun getDoubleLE(index: Int): Double {
        return buf.getDoubleLE(index)
    }

    override fun getBytes(index: Int, dst: ByteBuf): ByteBuf {
        return buf.getBytes(index, dst)
    }

    override fun getBytes(index: Int, dst: ByteBuf, length: Int): ByteBuf {
        return buf.getBytes(index, dst, length)
    }

    override fun getBytes(index: Int, dst: ByteBuf, dstIndex: Int, length: Int): ByteBuf {
        return buf.getBytes(index, dst, dstIndex, length)
    }

    override fun getBytes(index: Int, dst: ByteArray): ByteBuf {
        return buf.getBytes(index, dst)
    }

    override fun getBytes(index: Int, dst: ByteArray, dstIndex: Int, length: Int): ByteBuf {
        return buf.getBytes(index, dst, dstIndex, length)
    }

    override fun getBytes(index: Int, dst: ByteBuffer): ByteBuf {
        return buf.getBytes(index, dst)
    }

    @Throws(IOException::class)
    override fun getBytes(index: Int, out: OutputStream, length: Int): ByteBuf {
        return buf.getBytes(index, out, length)
    }

    @Throws(IOException::class)
    override fun getBytes(index: Int, out: GatheringByteChannel, length: Int): Int {
        return buf.getBytes(index, out, length)
    }

    @Throws(IOException::class)
    override fun getBytes(index: Int, out: FileChannel, position: Long, length: Int): Int {
        return buf.getBytes(index, out, position, length)
    }

    override fun getCharSequence(index: Int, length: Int, charset: Charset): CharSequence {
        return buf.getCharSequence(index, length, charset)
    }

    override fun setBoolean(index: Int, value: Boolean): ByteBuf {
        return buf.setBoolean(index, value)
    }

    override fun setByte(index: Int, value: Int): ByteBuf {
        return buf.setByte(index, value)
    }

    override fun setShort(index: Int, value: Int): ByteBuf {
        return buf.setShort(index, value)
    }

    override fun setShortLE(index: Int, value: Int): ByteBuf {
        return buf.setShortLE(index, value)
    }

    override fun setMedium(index: Int, value: Int): ByteBuf {
        return buf.setMedium(index, value)
    }

    override fun setMediumLE(index: Int, value: Int): ByteBuf {
        return buf.setMediumLE(index, value)
    }

    override fun setInt(index: Int, value: Int): ByteBuf {
        return buf.setInt(index, value)
    }

    override fun setIntLE(index: Int, value: Int): ByteBuf {
        return buf.setIntLE(index, value)
    }

    override fun setLong(index: Int, value: Long): ByteBuf {
        return buf.setLong(index, value)
    }

    override fun setLongLE(index: Int, value: Long): ByteBuf {
        return buf.setLongLE(index, value)
    }

    override fun setChar(index: Int, value: Int): ByteBuf {
        return buf.setChar(index, value)
    }

    override fun setFloat(index: Int, value: Float): ByteBuf {
        return buf.setFloat(index, value)
    }

    override fun setFloatLE(index: Int, value: Float): ByteBuf {
        return buf.setFloatLE(index, value)
    }

    override fun setDouble(index: Int, value: Double): ByteBuf {
        return buf.setDouble(index, value)
    }

    override fun setDoubleLE(index: Int, value: Double): ByteBuf {
        return buf.setDoubleLE(index, value)
    }

    override fun setBytes(index: Int, src: ByteBuf): ByteBuf {
        return buf.setBytes(index, src)
    }

    override fun setBytes(index: Int, src: ByteBuf, length: Int): ByteBuf {
        return buf.setBytes(index, src, length)
    }

    override fun setBytes(index: Int, src: ByteBuf, srcIndex: Int, length: Int): ByteBuf {
        return buf.setBytes(index, src, srcIndex, length)
    }

    override fun setBytes(index: Int, src: ByteArray): ByteBuf {
        return buf.setBytes(index, src)
    }

    override fun setBytes(index: Int, src: ByteArray, srcIndex: Int, length: Int): ByteBuf {
        return buf.setBytes(index, src, srcIndex, length)
    }

    override fun setBytes(index: Int, src: ByteBuffer): ByteBuf {
        return buf.setBytes(index, src)
    }

    @Throws(IOException::class)
    override fun setBytes(index: Int, `in`: InputStream, length: Int): Int {
        return buf.setBytes(index, `in`, length)
    }

    @Throws(IOException::class)
    override fun setBytes(index: Int, `in`: ScatteringByteChannel, length: Int): Int {
        return buf.setBytes(index, `in`, length)
    }

    @Throws(IOException::class)
    override fun setBytes(index: Int, `in`: FileChannel, position: Long, length: Int): Int {
        return buf.setBytes(index, `in`, position, length)
    }

    override fun setZero(index: Int, length: Int): ByteBuf {
        return buf.setZero(index, length)
    }

    override fun setCharSequence(index: Int, sequence: CharSequence, charset: Charset): Int {
        return buf.setCharSequence(index, sequence, charset)
    }

    override fun readBoolean(): Boolean {
        return buf.readBoolean()
    }

    override fun readByte(): Byte {
        return buf.readByte()
    }

    override fun readUnsignedByte(): Short {
        return buf.readUnsignedByte()
    }

    override fun readShort(): Short {
        return buf.readShort()
    }

    override fun readShortLE(): Short {
        return buf.readShortLE()
    }

    override fun readUnsignedShort(): Int {
        return buf.readUnsignedShort()
    }

    override fun readUnsignedShortLE(): Int {
        return buf.readUnsignedShortLE()
    }

    override fun readMedium(): Int {
        return buf.readMedium()
    }

    override fun readMediumLE(): Int {
        return buf.readMediumLE()
    }

    override fun readUnsignedMedium(): Int {
        return buf.readUnsignedMedium()
    }

    override fun readUnsignedMediumLE(): Int {
        return buf.readUnsignedMediumLE()
    }

    override fun readInt(): Int {
        return buf.readInt()
    }

    override fun readIntLE(): Int {
        return buf.readIntLE()
    }

    override fun readUnsignedInt(): Long {
        return buf.readUnsignedInt()
    }

    override fun readUnsignedIntLE(): Long {
        return buf.readUnsignedIntLE()
    }

    override fun readLong(): Long {
        return buf.readLong()
    }

    override fun readLongLE(): Long {
        return buf.readLongLE()
    }

    override fun readChar(): Char {
        return buf.readChar()
    }

    override fun readFloat(): Float {
        return buf.readFloat()
    }

    override fun readFloatLE(): Float {
        return buf.readFloatLE()
    }

    override fun readDouble(): Double {
        return buf.readDouble()
    }

    override fun readDoubleLE(): Double {
        return buf.readDoubleLE()
    }

    override fun readBytes(length: Int): ByteBuf {
        return buf.readBytes(length)
    }

    override fun readSlice(length: Int): ByteBuf {
        return buf.readSlice(length)
    }

    override fun readRetainedSlice(length: Int): ByteBuf {
        return buf.readRetainedSlice(length)
    }

    override fun readBytes(dst: ByteBuf): ByteBuf {
        return buf.readBytes(dst)
    }

    override fun readBytes(dst: ByteBuf, length: Int): ByteBuf {
        return buf.readBytes(dst, length)
    }

    override fun readBytes(dst: ByteBuf, dstIndex: Int, length: Int): ByteBuf {
        return buf.readBytes(dst, dstIndex, length)
    }

    override fun readBytes(dst: ByteArray): ByteBuf {
        return buf.readBytes(dst)
    }

    override fun readBytes(dst: ByteArray, dstIndex: Int, length: Int): ByteBuf {
        return buf.readBytes(dst, dstIndex, length)
    }

    override fun readBytes(dst: ByteBuffer): ByteBuf {
        return buf.readBytes(dst)
    }

    @Throws(IOException::class)
    override fun readBytes(out: OutputStream, length: Int): ByteBuf {
        return buf.readBytes(out, length)
    }

    @Throws(IOException::class)
    override fun readBytes(out: GatheringByteChannel, length: Int): Int {
        return buf.readBytes(out, length)
    }

    override fun readCharSequence(length: Int, charset: Charset): CharSequence {
        return buf.readCharSequence(length, charset)
    }

    @Throws(IOException::class)
    override fun readBytes(out: FileChannel, position: Long, length: Int): Int {
        return buf.readBytes(out, position, length)
    }

    override fun skipBytes(length: Int): ByteBuf {
        return buf.skipBytes(length)
    }

    override fun writeBoolean(value: Boolean): ByteBuf {
        return buf.writeBoolean(value)
    }

    override fun writeByte(value: Int): ByteBuf {
        return buf.writeByte(value)
    }

    override fun writeShort(value: Int): ByteBuf {
        return buf.writeShort(value)
    }

    override fun writeShortLE(value: Int): ByteBuf {
        return buf.writeShortLE(value)
    }

    override fun writeMedium(value: Int): ByteBuf {
        return buf.writeMedium(value)
    }

    override fun writeMediumLE(value: Int): ByteBuf {
        return buf.writeMediumLE(value)
    }

    override fun writeInt(value: Int): ByteBuf {
        return buf.writeInt(value)
    }

    override fun writeIntLE(value: Int): ByteBuf {
        return buf.writeIntLE(value)
    }

    override fun writeLong(value: Long): ByteBuf {
        return buf.writeLong(value)
    }

    override fun writeLongLE(value: Long): ByteBuf {
        return buf.writeLongLE(value)
    }

    override fun writeChar(value: Int): ByteBuf {
        return buf.writeChar(value)
    }

    override fun writeFloat(value: Float): ByteBuf {
        return buf.writeFloat(value)
    }

    override fun writeFloatLE(value: Float): ByteBuf {
        return buf.writeFloatLE(value)
    }

    override fun writeDouble(value: Double): ByteBuf {
        return buf.writeDouble(value)
    }

    override fun writeDoubleLE(value: Double): ByteBuf {
        return buf.writeDoubleLE(value)
    }

    override fun writeBytes(src: ByteBuf): ByteBuf {
        return buf.writeBytes(src)
    }

    override fun writeBytes(src: ByteBuf, length: Int): ByteBuf {
        return buf.writeBytes(src, length)
    }

    override fun writeBytes(src: ByteBuf, srcIndex: Int, length: Int): ByteBuf {
        return buf.writeBytes(src, srcIndex, length)
    }

    override fun writeBytes(src: ByteArray): ByteBuf {
        return buf.writeBytes(src)
    }

    override fun writeBytes(src: ByteArray, srcIndex: Int, length: Int): ByteBuf {
        return buf.writeBytes(src, srcIndex, length)
    }

    override fun writeBytes(src: ByteBuffer): ByteBuf {
        return buf.writeBytes(src)
    }

    @Throws(IOException::class)
    override fun writeBytes(`in`: InputStream, length: Int): Int {
        return buf.writeBytes(`in`, length)
    }

    @Throws(IOException::class)
    override fun writeBytes(`in`: ScatteringByteChannel, length: Int): Int {
        return buf.writeBytes(`in`, length)
    }

    @Throws(IOException::class)
    override fun writeBytes(`in`: FileChannel, position: Long, length: Int): Int {
        return buf.writeBytes(`in`, position, length)
    }

    override fun writeZero(length: Int): ByteBuf {
        return buf.writeZero(length)
    }

    override fun writeCharSequence(sequence: CharSequence, charset: Charset): Int {
        return buf.writeCharSequence(sequence, charset)
    }

    override fun indexOf(fromIndex: Int, toIndex: Int, value: Byte): Int {
        return buf.indexOf(fromIndex, toIndex, value)
    }

    override fun bytesBefore(value: Byte): Int {
        return buf.bytesBefore(value)
    }

    override fun bytesBefore(length: Int, value: Byte): Int {
        return buf.bytesBefore(length, value)
    }

    override fun bytesBefore(index: Int, length: Int, value: Byte): Int {
        return buf.bytesBefore(index, length, value)
    }

    override fun forEachByte(processor: ByteProcessor): Int {
        return buf.forEachByte(processor)
    }

    override fun forEachByte(index: Int, length: Int, processor: ByteProcessor): Int {
        return buf.forEachByte(index, length, processor)
    }

    override fun forEachByteDesc(processor: ByteProcessor): Int {
        return buf.forEachByteDesc(processor)
    }

    override fun forEachByteDesc(index: Int, length: Int, processor: ByteProcessor): Int {
        return buf.forEachByteDesc(index, length, processor)
    }

    override fun copy(): ByteBuf {
        return buf.copy()
    }

    override fun copy(index: Int, length: Int): ByteBuf {
        return buf.copy(index, length)
    }

    override fun slice(): ByteBuf {
        return buf.slice()
    }

    override fun retainedSlice(): ByteBuf {
        return buf.retainedSlice()
    }

    override fun slice(index: Int, length: Int): ByteBuf {
        return buf.slice(index, length)
    }

    override fun retainedSlice(index: Int, length: Int): ByteBuf {
        return buf.retainedSlice(index, length)
    }

    override fun duplicate(): ByteBuf {
        return buf.duplicate()
    }

    override fun retainedDuplicate(): ByteBuf {
        return buf.retainedDuplicate()
    }

    override fun nioBufferCount(): Int {
        return buf.nioBufferCount()
    }

    override fun nioBuffer(): ByteBuffer {
        return buf.nioBuffer()
    }

    override fun nioBuffer(index: Int, length: Int): ByteBuffer {
        return buf.nioBuffer(index, length)
    }

    override fun internalNioBuffer(index: Int, length: Int): ByteBuffer {
        return buf.internalNioBuffer(index, length)
    }

    override fun nioBuffers(): Array<ByteBuffer> {
        return buf.nioBuffers()
    }

    override fun nioBuffers(index: Int, length: Int): Array<ByteBuffer> {
        return buf.nioBuffers(index, length)
    }

    override fun hasArray(): Boolean {
        return buf.hasArray()
    }

    override fun array(): ByteArray {
        return buf.array()
    }

    override fun arrayOffset(): Int {
        return buf.arrayOffset()
    }

    override fun hasMemoryAddress(): Boolean {
        return buf.hasMemoryAddress()
    }

    override fun memoryAddress(): Long {
        return buf.memoryAddress()
    }

    override fun isContiguous(): Boolean {
        return buf.isContiguous
    }

    override fun toString(charset: Charset): String {
        return buf.toString(charset)
    }

    override fun toString(index: Int, length: Int, charset: Charset): String {
        return buf.toString(index, length, charset)
    }

    override fun hashCode(): Int {
        return buf.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return buf == other
    }

    override fun compareTo(other: ByteBuf): Int {
        return buf.compareTo(other)
    }

    override fun toString(): String {
        return buf.toString()
    }

    override fun retain(increment: Int): ByteBuf {
        return buf.retain(increment)
    }

    override fun retain(): ByteBuf {
        return buf.retain()
    }

    override fun touch(): ByteBuf {
        return buf.touch()
    }

    override fun touch(hint: Any): ByteBuf {
        return buf.touch(hint)
    }

    override fun refCnt(): Int {
        return buf.refCnt()
    }

    override fun release(): Boolean {
        return buf.release()
    }

    override fun release(decrement: Int): Boolean {
        return buf.release(decrement)
    }

    companion object {
        private fun writeFixedBitSet(bits: BitSet, size: Int, buf: ByteBuf) {
            if (bits.length() > size) {
                throw BitSetTooLargeException("BitSet too large (expected $size got ${bits.size()})")
            }
            buf.writeBytes(Arrays.copyOf(bits.toByteArray(), size + 8 shr 3))
        }

        fun create(): ByteMessage { return ByteMessage(
            Unpooled.buffer()
        )
        }
    }
}
