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

package catmoe.fallencrystal.moefilter.network.bungee.limbo.packets.player

import catmoe.fallencrystal.moefilter.network.bungee.limbo.packets.util.MoeAbstractPacket
import io.netty.buffer.ByteBuf
import net.md_5.bungee.protocol.ProtocolConstants
import se.llbit.nbt.CompoundTag
import se.llbit.nbt.IntTag
import se.llbit.nbt.NamedTag

@Suppress("MemberVisibilityCanBePrivate", "unused")
class SetSlot(val windowId: Int, val slot: Int, val item: Int, val count: Int, val data: Int) : MoeAbstractPacket() {

    override fun write(buf: ByteBuf, direction: ProtocolConstants.Direction, version: Int) {
        buf.writeByte(windowId)

        if (version >= ProtocolConstants.MINECRAFT_1_17_1) { writeVarInt(0, buf) }
        buf.writeShort(slot)
        val id = if (item == 358) getCaptchaId(version) else item
        val present = id > 0
        if (version >= ProtocolConstants.MINECRAFT_1_13_2) { buf.writeBoolean(present) }
        if (!present && version < ProtocolConstants.MINECRAFT_1_13_2) { buf.writeShort(-1) }
        if (present) {
            if (version < ProtocolConstants.MINECRAFT_1_13_2) { buf.writeShort(id) } else { writeVarInt(id, buf) }
            buf.writeByte(count)
            if (version < ProtocolConstants.MINECRAFT_1_13) { buf.writeShort(data) }
            if (version < ProtocolConstants.MINECRAFT_1_17) { buf.writeByte(0)
            } else {
                val nbt = CompoundTag()
                nbt.add("map", IntTag(0))
                writeTag(NamedTag("", nbt), buf)
            }
        }
    }

    private fun getCaptchaId(version: Int): Int {
        return if (version <= ProtocolConstants.MINECRAFT_1_12_2) { 358 } else if (version == ProtocolConstants.MINECRAFT_1_13) { 608
        } else if (version <= ProtocolConstants.MINECRAFT_1_13_2) { 613 } else if (version <= ProtocolConstants.MINECRAFT_1_15_2) { 671
        } else if (version <= ProtocolConstants.MINECRAFT_1_16_4) { 733 } else if (version <= ProtocolConstants.MINECRAFT_1_18_2) { 847
        } else if (version <= ProtocolConstants.MINECRAFT_1_19_1) { 886 } else if (version <= ProtocolConstants.MINECRAFT_1_19_3) { 914
        } else if (version <= ProtocolConstants.MINECRAFT_1_19_4) { 937 } else { 941 }
    }

}