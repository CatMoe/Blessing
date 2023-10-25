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

package catmoe.fallencrystal.moefilter.network.limbo.handler.ping

import catmoe.fallencrystal.moefilter.network.limbo.handler.MoeLimbo.debug
import catmoe.fallencrystal.moefilter.network.common.ByteMessage
import catmoe.fallencrystal.moefilter.network.limbo.packet.s2c.PacketPingResponse
import catmoe.fallencrystal.translation.utils.version.Version
import io.netty.buffer.Unpooled

class CachedMotd(
    val packet: PacketPingResponse,
    val bm: ByteArray,
    val bmNoIcon: ByteArray,
    val version: Version,
) {
    companion object {
        fun process(packet: PacketPingResponse, version: Version): CachedMotd {
            val raw = packet.output ?: throw NullPointerException("Output cannot be null!")
            val bm1 = ByteMessage(Unpooled.buffer())
            debug(raw)
            packet.encode(bm1, version)
            val array1 = bm1.toByteArray()
            bm1.release()
            val noIcon = raw.replace(""","favicon":"data:(.*?)"""".toRegex(), "")
            val array2: ByteArray
            if (noIcon == raw) { array2=array1; debug("NoIcon output is equal have icon (Original motd may don't have icon)") } else {
                val bm2 = ByteMessage(Unpooled.buffer())
                debug(noIcon)
                packet.output=noIcon
                packet.encode(bm2, version)
                array2 = bm2.toByteArray()
                bm2.release()
                packet.output=raw
            }
            return CachedMotd(packet, array1, array2, version)
        }
    }
}