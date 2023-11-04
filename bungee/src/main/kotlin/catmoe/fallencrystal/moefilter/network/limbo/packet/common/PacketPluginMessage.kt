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

import catmoe.fallencrystal.moefilter.check.brand.BrandCheck
import catmoe.fallencrystal.moefilter.check.info.impl.Brand
import catmoe.fallencrystal.moefilter.network.common.ByteMessage
import catmoe.fallencrystal.moefilter.network.common.kick.DisconnectType
import catmoe.fallencrystal.moefilter.network.common.kick.FastDisconnect
import catmoe.fallencrystal.moefilter.network.limbo.handler.LimboHandler
import catmoe.fallencrystal.moefilter.network.limbo.handler.MoeLimbo
import catmoe.fallencrystal.moefilter.network.limbo.packet.LimboPacket
import catmoe.fallencrystal.translation.utils.version.Version
import com.google.common.base.Preconditions
import io.netty.buffer.Unpooled
import io.netty.channel.Channel

// 注: 我仍在研究1.7的PluginMessage, 因此对此暂时禁用
class PacketPluginMessage : LimboPacket {


    var channel = ""
    var message = ""

    var data: ByteArray? = null


    override fun encode(packet: ByteMessage, version: Version?) {
        listOf(this.channel, this.message).forEach { packet.writeString(it) }
    }

    override fun decode(packet: ByteMessage, channel: Channel, version: Version?) {
        this.channel = packet.readString()
        if (version == Version.V1_7_6) packet.readShort() // Ignored
        Preconditions.checkArgument(packet.readableBytes() < 32767, "Payload is too large")
        this.data=ByteArray(packet.readableBytes())
        if (version?.moreOrEqual(Version.V1_8) == true && (this.channel == "MC|Brand" || this.channel == "minecraft:brand")) this.message=decodeBrand(packet)
    }

    override fun handle(handler: LimboHandler) {
        if (this.channel == "MC|Brand" || this.channel == "minecraft:brand") {
            handler.brand=this.message
            if (BrandCheck.increase(Brand(this.message))) {
                FastDisconnect.disconnect(handler, DisconnectType.BRAND_NOT_ALLOWED)
            }
            MoeLimbo.debug(handler, "Brand: $message")
        }
    }

    override fun toString(): String {
        return "PacketPluginMessage(channel=$channel, message=$message)"
    }

    companion object {
        @Suppress("MemberVisibilityCanBePrivate")
        fun decodeBrand(byteBuf: ByteMessage): String {
            val data = ByteArray(byteBuf.readableBytes())
            byteBuf.readBytes(data)
            val b = ByteMessage(Unpooled.wrappedBuffer(data))
            val message = b.readString()
            b.release()
            Preconditions.checkArgument(!(message.isEmpty() || message.length > 128 || message.contains("jndi")), "Invalid brand data.")
            return message
        }
    }

}