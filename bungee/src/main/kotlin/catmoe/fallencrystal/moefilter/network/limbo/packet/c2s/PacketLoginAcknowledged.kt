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
import catmoe.fallencrystal.moefilter.network.limbo.handler.LimboHandler
import catmoe.fallencrystal.moefilter.network.limbo.packet.ExplicitPacket
import catmoe.fallencrystal.moefilter.network.limbo.packet.LimboC2SPacket
import catmoe.fallencrystal.moefilter.network.limbo.packet.cache.EnumPacket
import catmoe.fallencrystal.moefilter.network.limbo.packet.common.PacketFinishConfiguration
import catmoe.fallencrystal.moefilter.network.limbo.packet.protocol.Protocol
import catmoe.fallencrystal.moefilter.network.limbo.packet.s2c.RegistryData
import catmoe.fallencrystal.translation.utils.version.Version
import io.netty.channel.Channel

class PacketLoginAcknowledged : LimboC2SPacket() {
    override fun decode(packet: ByteMessage, channel: Channel, version: Version?) {
        // This packet does not have any field.
    }

    override fun handle(handler: LimboHandler) {
        val version = handler.version!!
        handler.updateVersion(version, Protocol.CONFIGURATION)
        handler.writePacket(EnumPacket.PLUGIN_MESSAGE)
        handler.writePacket(ExplicitPacket(0x05, RegistryData.getCachedRegistry(version), "Cached Registry data."))
        //handler.writePacket(PacketPingPong())
        handler.sendPacket(PacketFinishConfiguration())
    }
}