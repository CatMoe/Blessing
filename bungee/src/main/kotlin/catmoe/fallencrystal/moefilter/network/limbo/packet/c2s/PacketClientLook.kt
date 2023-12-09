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

// That packet is for legacy clients.
@Suppress("MemberVisibilityCanBePrivate")
class PacketClientLook(var lastLook: LimboLocation? = null) : LimboC2SPacket() {

    override fun decode(byteBuf: ByteMessage, channel: Channel, version: Version?) {
        lastLook = LimboLocation(
            0.0, 0.0, 0.0,
            byteBuf.readFloat(), byteBuf.readFloat(),
            byteBuf.readBoolean()
        )
    }

    override fun handle(handler: LimboHandler) {
        val loc = handler.location
        val lastLok = lastLook ?: return
        if (loc == null) handler.location=lastLook
        else LimboLocation(
            loc.x, loc.y, loc.z,
            lastLok.yaw, lastLok.pitch,
            lastLok.onGround
        )
    }

    override fun toString() = "PacketClientLook(look=$lastLook)"
}