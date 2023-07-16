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
package catmoe.fallencrystal.moefilter.network.bungee.limbo.packets.limbo

import catmoe.fallencrystal.moefilter.network.bungee.limbo.packets.util.MoeAbstractPacket
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufOutputStream
import net.md_5.bungee.protocol.ProtocolConstants
import java.io.IOException
import java.util.*

@Suppress("unused")
class EmptyChunk(private val x: Int, private val z: Int) : MoeAbstractPacket() {
    override fun write(buf: ByteBuf, direction: ProtocolConstants.Direction, version: Int) {
        buf.writeInt(x)
        buf.writeInt(z)
        if (version < ProtocolConstants.MINECRAFT_1_17) { buf.writeBoolean(true) }
        if (version >= ProtocolConstants.MINECRAFT_1_16 && version < ProtocolConstants.MINECRAFT_1_16_2) { buf.writeBoolean(true) }

        //BitMasks
        if (version < ProtocolConstants.MINECRAFT_1_17) {
            if (version == ProtocolConstants.MINECRAFT_1_8) { buf.writeShort(1) } else { writeVarInt(0, buf) }
        } else if (version < ProtocolConstants.MINECRAFT_1_18) {
            val bitSet = BitSet()
            (0..15).forEach { bitSet[it]=false }
            val mask = bitSet.toLongArray()
            writeVarInt(mask.size, buf)
            mask.forEach { buf.writeLong(it) }
        }
        if (version >= ProtocolConstants.MINECRAFT_1_14) {
            try {
                ByteBufOutputStream(buf).use { output ->
                    output.writeByte(10) //CompoundTag
                    output.writeUTF("") // CompoundName
                    output.writeByte(10) //CompoundTag
                    output.writeUTF("root") //root compound
                    output.writeByte(12) //long array
                    output.writeUTF("MOTION_BLOCKING")
                    val longArrayTag = LongArray(if (version < ProtocolConstants.MINECRAFT_1_18) 36 else 37)
                    output.writeInt(longArrayTag.size)
                    var i = 0
                    val length = longArrayTag.size
                    while (i < length) { output.writeLong(longArrayTag[i]); i++ }
                    buf.writeByte(0) //end of compound
                    buf.writeByte(0) //end of compound
                }
            } catch (ex: IOException) { throw UnsupportedOperationException(ex) }
            if (version >= ProtocolConstants.MINECRAFT_1_15 && version < ProtocolConstants.MINECRAFT_1_18) {
                if (version >= ProtocolConstants.MINECRAFT_1_16_2) { writeVarInt(1024, buf);
                    (0..1023).forEach { _ -> writeVarInt(1, buf) }
                } else { (0..1023).forEach { _ -> buf.writeInt(0) } }
            }
        }
        if (version < ProtocolConstants.MINECRAFT_1_13) { writeArray(ByteArray(256), buf) /* 1.8 - 1.12.2 */
        } else if (version < ProtocolConstants.MINECRAFT_1_15) { writeArray(ByteArray(1024), buf) /* 1.13 - 1.14.4 */
        } else if (version < ProtocolConstants.MINECRAFT_1_18) { writeVarInt(0, buf) /* 1.15 - 1.17.1 */ } else {
            val sectionData = byteArrayOf(0, 0, 0, 0, 0, 0, 1, 0)
            writeVarInt(sectionData.size * 16, buf)
            (0..15).forEach { _ -> buf.writeBytes(sectionData) }
        }
        if (version >= ProtocolConstants.MINECRAFT_1_9_4) { writeVarInt(0, buf) }
        if (version >= ProtocolConstants.MINECRAFT_1_18) /* light data */ {
            val lightData = byteArrayOf(1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 3, -1, -1, 0, 0)
            buf.ensureWritable(lightData.size)
            if (version >= ProtocolConstants.MINECRAFT_1_20) { buf.writeBytes(lightData, 1, lightData.size - 1) } else { buf.writeBytes(lightData) }
        }
    }
}