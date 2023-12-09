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
package catmoe.fallencrystal.moefilter.network.limbo.packet.cache

import catmoe.fallencrystal.moefilter.network.common.ByteMessage
import catmoe.fallencrystal.moefilter.network.limbo.packet.LimboPacket
import catmoe.fallencrystal.moefilter.network.limbo.packet.LimboS2CPacket
import catmoe.fallencrystal.translation.utils.version.Version
import io.netty.buffer.Unpooled
import java.util.*

class PacketSnapshot(val wrappedPacket: LimboPacket) : LimboS2CPacket() {
    private val versionMessages: MutableMap<Version, ByteArray> = EnumMap(Version::class.java)
    private val mappings: MutableMap<Version, Version> = EnumMap(Version::class.java)
    fun encode() {
        val hashes: MutableMap<Int, Version> = HashMap()
        for (version in Version.entries) {
            if (version == Version.UNDEFINED) continue
            val encodedMessage = ByteMessage(Unpooled.buffer())
            wrappedPacket.encode(encodedMessage, version)
            val hash = encodedMessage.hashCode()
            val hashed = hashes[hash]
            if (hashed != null) {
                mappings[version] = hashed
            } else {
                hashes[hash] = version
                mappings[version] = version
                versionMessages[version] = encodedMessage.toByteArray()
            }
            encodedMessage.release()
        }
    }

    override fun encode(byteBuf: ByteMessage, version: Version?) {
        val mapped = mappings[version]
        val message = versionMessages[mapped] ?: throw IllegalArgumentException("No mappings for version $version")
        byteBuf.writeBytes(message)
    }

    companion object {
        fun of(packet: LimboPacket): PacketSnapshot {
            val snapshot = PacketSnapshot(packet)
            snapshot.encode()
            return snapshot
        }
    }
}
