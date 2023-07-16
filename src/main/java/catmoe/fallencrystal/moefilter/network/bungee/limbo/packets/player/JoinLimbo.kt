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

import catmoe.fallencrystal.moefilter.network.bungee.limbo.dimension.Dimension
import catmoe.fallencrystal.moefilter.network.bungee.limbo.packets.util.MoeAbstractPacket
import catmoe.fallencrystal.moefilter.network.bungee.limbo.packets.util.UnsupportedPacketOperationException
import io.netty.buffer.ByteBuf
import net.md_5.bungee.protocol.ProtocolConstants


@Suppress("unused",  "MemberVisibilityCanBePrivate")
class JoinLimbo(
    val entityId: Int,
    val dimension: Dimension
) : MoeAbstractPacket() {
    private val hardcore = true
    private val gameMode: Short = 0
    private val previousGameMode: Short = 0
    private val worldNames: Set<String> = HashSet(listOf(dimension.key))
    private val worldName = dimension.key
    private val dimensionId = dimension.dimensionId
    private val seed: Long = 1
    private val difficulty: Short = 0
    private val maxPlayers: Short = 1
    private val levelType = "flat"
    private val viewDistance = 0
    private val reducedDebugInfo = false
    private val normalRespawn = true
    private val debug = false
    private val flat = true

    override fun read(i1: ByteBuf?, i2: ProtocolConstants.Direction?, i3: Int) { throw UnsupportedPacketOperationException() }

    override fun write(buf: ByteBuf, direction: ProtocolConstants.Direction?, protocolVersion: Int) {
        buf.writeInt(entityId)
        if (protocolVersion >= ProtocolConstants.MINECRAFT_1_16_2) { buf.writeBoolean(hardcore) }
        buf.writeByte(gameMode.toInt())
        if (protocolVersion >= ProtocolConstants.MINECRAFT_1_16) {
            buf.writeByte(previousGameMode.toInt())
            writeVarInt(worldNames.size, buf)
            worldNames.forEach { writeString(it, buf) }
            writeTag(dimension.getFullCodec(protocolVersion), buf)
        }
        if (protocolVersion >= ProtocolConstants.MINECRAFT_1_16) {
            if (protocolVersion >= ProtocolConstants.MINECRAFT_1_19 || protocolVersion <= ProtocolConstants.MINECRAFT_1_16_1) {
                writeString(worldName, buf) } else { writeTag(dimension.getAttributes(protocolVersion), buf) }
            writeString(worldName, buf)
        } else if (protocolVersion > ProtocolConstants.MINECRAFT_1_9) { buf.writeInt(dimensionId) } else { buf.writeByte(dimensionId) }
        if (protocolVersion >= ProtocolConstants.MINECRAFT_1_15) { buf.writeLong(seed) }
        if (protocolVersion < ProtocolConstants.MINECRAFT_1_14) { buf.writeByte(difficulty.toInt()) }
        if (protocolVersion >= ProtocolConstants.MINECRAFT_1_16_2) { writeVarInt(maxPlayers.toInt(), buf) } else { buf.writeByte(maxPlayers.toInt()) }
        if (protocolVersion < ProtocolConstants.MINECRAFT_1_16) { writeString(levelType, buf) }
        if (protocolVersion >= ProtocolConstants.MINECRAFT_1_14) { writeVarInt(viewDistance, buf) }
        if (protocolVersion >= ProtocolConstants.MINECRAFT_1_18) { writeVarInt(viewDistance, buf) }
        if (protocolVersion >= 29) { buf.writeBoolean(reducedDebugInfo) }
        if (protocolVersion >= ProtocolConstants.MINECRAFT_1_15) { buf.writeBoolean(normalRespawn) }
        if (protocolVersion >= ProtocolConstants.MINECRAFT_1_16) { buf.writeBoolean(debug); buf.writeBoolean(flat) }
        if (protocolVersion >= ProtocolConstants.MINECRAFT_1_19) { buf.writeBoolean(false) }
        if (protocolVersion >= ProtocolConstants.MINECRAFT_1_20) { writeVarInt(0, buf) }
    }


}