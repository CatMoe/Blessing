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

package catmoe.fallencrystal.moefilter.network.bungee.limbo.packet.s2c

import catmoe.fallencrystal.moefilter.network.bungee.limbo.packet.ByteMessage
import catmoe.fallencrystal.moefilter.network.bungee.limbo.packet.LimboS2CPacket
import catmoe.fallencrystal.moefilter.network.bungee.limbo.util.Version
import io.netty.buffer.ByteBufOutputStream
import java.util.*


class PacketEmptyChunk : LimboS2CPacket() {

    var x: Int = 0
    var z: Int = 0

    override fun encode(packet: ByteMessage, version: Version?) {
        listOf(x, z).forEach { packet.writeInt(it) }
        if (version!!.more(Version.V1_17)) packet.writeBoolean(true)
        if (version.moreOrEqual(Version.V1_16) && version.less(Version.V1_16_2)) packet.writeBoolean(true)
        if (version.less(Version.V1_17)) {
            if (version.lessOrEqual(Version.V1_8)) packet.writeShort(1) else packet.writeVarInt(0)
        } else if (version.more(Version.V1_18)) {
            val bitSet = BitSet()
            for (it in 0..15) { bitSet[it] = false }
            val mask = bitSet.toLongArray()
            packet.writeVarInt(mask.size)
            mask.forEach { packet.writeLong(it) }
        }
        if (version.moreOrEqual(Version.V1_14)) {
            val output = ByteBufOutputStream(packet)
            output.writeByte( 10 ) //CompoundTag
            output.writeUTF( "" ) // CompoundName
            output.writeByte( 10 ) //CompoundTag
            output.writeUTF( "root" ) //root compound
            output.writeByte( 12 ) //long array
            output.writeUTF( "MOTION_BLOCKING" )
            val arrayTag = arrayOf((if (version.more(Version.V1_18)) 36 else 37).toLong())
            output.writeInt(arrayTag.size)
            for (element in arrayTag) { packet.writeLong(element) }
            listOf(0, 0).forEach { _ -> packet.writeByte(0) }
            if (version.moreOrEqual(Version.V1_15) && version.less(Version.V1_18)) {
                if (version.moreOrEqual(Version.V1_16_2)) {
                    packet.writeVarInt(1024)
                    (0..1023).forEach { _ -> packet.writeVarInt(1) }
                } else {  (0..1023).forEach { _ -> packet.writeInt(0) } }
            }
            if (version.less(Version.V1_13)) { packet.writeBytesArray(ByteArray(256))
            } else if (version.less(Version.V1_15)) { packet.writeBytesArray(ByteArray(1024))
            } else if (version.less(Version.V1_18)) { packet.writeVarInt(0)
            } else {
                val sectionData = byteArrayOf(0, 0, 0, 0, 0, 0, 1, 0)
                packet.writeVarInt(sectionData.size * 16)
                (0..15).forEach { _ -> packet.writeBytes(sectionData) }
            }
            if (version.moreOrEqual(Version.V1_9_4)) packet.writeVarInt(0)
            if (version.moreOrEqual(Version.V1_18)) {
                val lightData = byteArrayOf(1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 3, -1, -1, 0, 0)
                packet.ensureWritable(lightData.size)
                if (version.moreOrEqual(Version.V1_20)) {
                    packet.writeBytes(lightData, 1, lightData.size - 1)
                } else {
                    packet.writeBytes(lightData)
                }
            }
        }
    }
}