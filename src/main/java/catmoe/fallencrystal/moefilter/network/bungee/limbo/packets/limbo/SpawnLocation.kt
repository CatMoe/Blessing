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
import net.md_5.bungee.protocol.ProtocolConstants

@Suppress("MemberVisibilityCanBePrivate", "unused")
class SpawnLocation(val x: Long, val y: Long, val z: Long, val angle: Float) : MoeAbstractPacket() {

    override fun write(buf: ByteBuf, direction: ProtocolConstants.Direction, protocolVersion: Int) {
        val location: Long = if (protocolVersion < ProtocolConstants.MINECRAFT_1_14) {
            x and 0x3FFFFFFL shl 38 or (y and 0xFFFL shl 26) or (z and 0x3FFFFFFL)
        } else {
            x and 0x3FFFFFFL shl 38 or (z and 0x3FFFFFFL shl 12) or (y and 0xFFFL)
        }
        buf.writeLong(location)
        if (protocolVersion >= ProtocolConstants.MINECRAFT_1_17) { buf.writeFloat(angle) }
    }
}
