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

package catmoe.fallencrystal.moefilter.network.limbo.packet.c2s

import catmoe.fallencrystal.moefilter.network.common.ByteMessage
import catmoe.fallencrystal.moefilter.network.limbo.LimboLocation
import catmoe.fallencrystal.moefilter.network.limbo.handler.LimboHandler
import catmoe.fallencrystal.moefilter.network.limbo.packet.LimboC2SPacket
import catmoe.fallencrystal.translation.utils.version.Version
import io.netty.channel.Channel

class PacketClientPosition : LimboC2SPacket() {

    var loc: LimboLocation? = null

    override fun decode(packet: ByteMessage, channel: Channel, version: Version?) {
        val x = packet.readDouble()
        val y = packet.readDouble()
        if (version == Version.V1_7_6) packet.readDouble() // Head y. Deprecated
        val z = packet.readDouble()
        loc = LimboLocation(
            x, y, z,
            0f, 0f, // PlayerPosition don't have any method to read yaw & pitch.
            packet.readBoolean() // onGround
        )
    }

    override fun handle(handler: LimboHandler) {
        val loc = handler.location
        val lastLoc = this.loc ?: return
        handler.location = if (loc == null) lastLoc
        else LimboLocation(
            lastLoc.x, lastLoc.y, lastLoc.z,
            loc.yaw, loc.pitch,
            lastLoc.onGround
        )
    }

    override fun toString(): String { return "PacketClientPosition(location=$loc)" }
}