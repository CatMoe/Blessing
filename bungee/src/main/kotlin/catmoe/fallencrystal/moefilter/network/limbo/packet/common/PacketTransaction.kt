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
import catmoe.fallencrystal.moefilter.network.limbo.packet.LimboPacket
import catmoe.fallencrystal.translation.utils.version.Version
import io.netty.channel.Channel
import kotlin.random.Random

/*
This packet is mixed PingPong (higher or equal 1.17 version) and WindowConfirmation.
Refs:
https://github.com/GrimAnticheat/Grim/blob/2.0/src/main/java/ac/grim/grimac/player/GrimPlayer.java#L354
https://wiki.vg/Protocol#Ping_.28play.29 (For 764 protocol pages)
https://github.com/jonesdevelopment/sonar/blob/main/sonar-common/src/main/java/xyz/jonesdev/sonar/common/fallback/FallbackVerificationHandler.java#L131
https://github.com/jonesdevelopment/sonar/blob/main/sonar-common/src/main/java/xyz/jonesdev/sonar/common/fallback/protocol/packets/play/Transaction.java
 */
class PacketTransaction(
    var id: Int = Random.nextInt(-32768, 32767),
    @Deprecated("This field is useless for limbo and 1.17+ clients.")
    var windowId: Byte = 0,
    @Deprecated("This field is useless for limbo and 1.17+ clients.")
    var accepted: Boolean = true
) : LimboPacket {

    @Suppress("DEPRECATION")
    override fun encode(byteBuf: ByteMessage, version: Version?) {
        if (version!!.moreOrEqual(Version.V1_17)) byteBuf.writeInt(id)
        else {
            byteBuf.writeByte(windowId.toInt())
            byteBuf.writeShort(id)
            byteBuf.writeBoolean(accepted)
        }
    }

    @Suppress("DEPRECATION")
    override fun decode(byteBuf: ByteMessage, channel: Channel, version: Version?) {
        if (version!!.moreOrEqual(Version.V1_17)) {
            id = byteBuf.readInt()
            accepted=true
        } else {
            windowId=byteBuf.readByte()
            id=byteBuf.readShort().toInt()
            accepted=byteBuf.readBoolean()
        }
    }

}