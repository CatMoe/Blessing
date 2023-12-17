/*
 * Copyright (C) 2023-2023. CatMoe / MoeFilter Contributors
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

package catmoe.fallencrystal.moefilter.network.limbo.packet.common

import catmoe.fallencrystal.moefilter.network.common.ByteMessage
import catmoe.fallencrystal.moefilter.network.limbo.handler.LimboHandler
import catmoe.fallencrystal.moefilter.network.limbo.packet.LimboPacket
import catmoe.fallencrystal.moefilter.network.limbo.packet.protocol.Protocol
import catmoe.fallencrystal.translation.utils.version.Version
import io.netty.channel.Channel

class PacketFinishConfiguration : LimboPacket {
    override fun encode(byteBuf: ByteMessage, version: Version?) {
        // This packet does not have any field.
    }

    override fun decode(byteBuf: ByteMessage, channel: Channel, version: Version?) {
        // This packet does not have any field.
    }

    override fun handle(handler: LimboHandler) {
        handler.updateVersion(handler.version, Protocol.PLAY)
        handler.sendPlayPackets()
    }
}