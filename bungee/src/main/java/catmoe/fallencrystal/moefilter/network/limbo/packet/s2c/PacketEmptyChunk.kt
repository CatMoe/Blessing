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

package catmoe.fallencrystal.moefilter.network.limbo.packet.s2c

import catmoe.fallencrystal.moefilter.network.limbo.netty.ByteMessage
import catmoe.fallencrystal.moefilter.network.limbo.packet.LimboS2CPacket
import catmoe.fallencrystal.translation.utils.version.Version
import io.netty.buffer.Unpooled
import se.llbit.nbt.CompoundTag
import se.llbit.nbt.LongArrayTag
import se.llbit.nbt.NamedTag
import java.util.*
import java.util.zip.Deflater


class PacketEmptyChunk : LimboS2CPacket() {

    var x: Int = 0
    var z: Int = 0


    @Suppress("GrazieInspection")
    override fun encode(packet: ByteMessage, version: Version?) {
        // Chunk pos
        packet.writeInt(x)
        packet.writeInt(z)
        if (version!!.less(Version.V1_17) /*  || version.fromTo(Version.V1_16, Version.V1_16_1) || version == Version.V1_7_6 */) packet.writeBoolean(true)
        if (version.less(Version.V1_17)) {
            if (version.lessOrEqual(Version.V1_8)) packet.writeShort(1) else packet.writeVarInt(0)
            if (version == Version.V1_7_6) {
                // some1 can help me...? I don't know how to send a readable chunk data for a 9yrs ago minecraft client.

                packet.writeShort(1)
                val byArray = /* byteArrayOf(63) */ ByteArray(256)
                val deflate = Deflater(9)
                deflate.setInput(byArray)
                deflate.finish()
                val compressed = Unpooled.buffer()
                val b = ByteArray(1024)
                while (!deflate.finished()) compressed.writeBytes(b, 0, deflate.deflate(b))
                packet.writeInt(compressed.readableBytes())
                packet.writeBytes(compressed)
                deflate.end()
                compressed.release()
                return

                /*
                packet.writeShort(1)
                val byArray = byteArrayOf(63)
                packet.writeInt(byArray.size)
                packet.writeBytes(byArray)
                return
                 */
                /*
                packet.writeShort(0)
                val uncompressed = byteArrayOf(256.toByte())
                val compressed = Unpooled.buffer()
                val deflate = Deflater(9)
                try {
                    deflate.setInput(uncompressed)
                    deflate.finish()
                    val buffer = ByteArray(1024)
                    while (deflate.finished()) {
                        val count: Int = deflate.deflate(buffer)
                        compressed.writeBytes(buffer, 0, count)
                    }
                    packet.writeInt(compressed.readableBytes())
                    packet.writeBytes(compressed)
                } finally {
                    deflate.end()
                    compressed.release()
                }
                 */
            }
        } else if (version.less(Version.V1_18)) {
            val bitSet = BitSet()
            for (it in 0..15) { bitSet[it] = false }
            val mask = bitSet.toLongArray()
            packet.writeVarInt(mask.size)
            mask.forEach { packet.writeLong(it) }
        }
        if (version.moreOrEqual(Version.V1_14)) {
            // Height maps << Start
            /*
            ByteBufOutputStream(packet).use { output ->
                output.writeByte(10) //CompoundTag
                output.writeUTF("") // CompoundName
                output.writeByte(10) //CompoundTag
                output.writeUTF("root") //root compound
                output.writeByte(12) //long array
                output.writeUTF("MOTION_BLOCKING")
                val arrayTag = LongArray(if (version.less(Version.V1_18)) 36 else 37)
                output.writeInt(arrayTag.size)
                var c = 0
                while (c < arrayTag.size) {
                    packet.writeLong(arrayTag[c])
                    c++
                }
                packet.writeByte(0)
                packet.writeByte(0)
            }
             */
            val arrayData = LongArray(if (version.less(Version.V1_18)) 36 else 37)
            val longArrayTag = LongArrayTag(arrayData)
            val tag = CompoundTag()
            tag.add("MOTION_BLOCKING", longArrayTag)
            val rootTag = CompoundTag()
            rootTag.add("root", tag)
            if (version.moreOrEqual(Version.V1_20_2)) {
                //DefinedPacket.writeNamelessTag(rootTag, buf)
                packet.writeHeadlessCompoundTag(rootTag)
            } else {
                //DefinedPacket.writeTag(NamedTag("", rootTag), buf, version)
                packet.writeCompoundTag(NamedTag("", rootTag))
            }
            // Height maps >> End
            if (version.fromTo(Version.V1_15_2, Version.V1_17_1)) {
                if (version.moreOrEqual(Version.V1_16_2)) {
                    packet.writeVarInt(1024)
                    (0..1023).forEach { _ -> packet.writeVarInt(1) }
                } else {  (0..1023).forEach { _ -> packet.writeInt(0) } }
            }
        }
        /*
        if (version.less(Version.V1_13))
            //packet.writeBytesArray(ByteArray(256))
            packet.writeBytesArray(ByteArray((256)))
        else if (version.less(Version.V1_15)) packet.writeBytesArray(ByteArray(1024))
        else if (version.less(Version.V1_18)) packet.writeVarInt(0)
        else {
            val sectionData = byteArrayOf(0, 0, 0, 0, 0, 0, 1, 0)
            packet.writeVarInt(sectionData.size * 16)
            (0..15).forEach { _ -> packet.writeBytes(sectionData) }
        }
         */
        when {
            version.less(Version.V1_13) -> packet.writeBytesArray(ByteArray((256)))
            version.less(Version.V1_15) -> packet.writeBytesArray(ByteArray(1024))
            version.less(Version.V1_18) -> packet.writeVarInt(0)
            else -> {
                val sectionData = byteArrayOf(0, 0, 0, 0, 0, 0, 1, 0)
                packet.writeVarInt(sectionData.size * 16)
                (0..15).forEach { _ -> packet.writeBytes(sectionData) }
            }
        }
        if (version.moreOrEqual(Version.V1_9_4)) packet.writeVarInt(0)
        if (version.moreOrEqual(Version.V1_18)) { // Light data
            val lightData = byteArrayOf(1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 3, -1, -1, 0, 0)
            packet.ensureWritable(lightData.size)
            if (version.moreOrEqual(Version.V1_20)) packet.writeBytes(lightData, 1, lightData.size - 1)
            else packet.writeBytes(lightData)
        }
    }


    override fun toString(): String { return "PacketEmptyChunk(TargetX=$x, TargetZ=$z)" }
}