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

package catmoe.fallencrystal.moefilter.network.limbo.handler.ping

import catmoe.fallencrystal.moefilter.network.limbo.handler.MoeLimbo.debug
import catmoe.fallencrystal.moefilter.network.limbo.netty.ByteMessage
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