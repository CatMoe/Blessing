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

import catmoe.fallencrystal.moefilter.network.limbo.handler.LimboHandler
import catmoe.fallencrystal.moefilter.network.common.ByteMessage
import catmoe.fallencrystal.moefilter.network.limbo.packet.LimboPacket
import catmoe.fallencrystal.translation.utils.version.Version
import io.netty.channel.Channel
import io.netty.channel.ChannelFutureListener

class PacketStatusPing : LimboPacket {

    private var randomId: Long? = null

    override fun encode(packet: ByteMessage, version: Version?) {
        packet.writeLong(randomId ?: 0)
    }

    override fun decode(packet: ByteMessage, channel: Channel, version: Version?) {
        randomId = packet.readLong()
    }

    override fun handle(handler: LimboHandler) {
        handler.channel.writeAndFlush(this).addListener(ChannelFutureListener.CLOSE)
    }
}