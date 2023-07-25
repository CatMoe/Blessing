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
package catmoe.fallencrystal.moefilter.network.bungee.limbo.packet

import catmoe.fallencrystal.moefilter.network.bungee.limbo.util.Version
import io.netty.buffer.Unpooled
import java.util.*

@Suppress("unused")
class PacketSnapshot(val wrappedPacket: LimboS2CPacket) : LimboS2CPacket() {
    private val versionMessages: MutableMap<Version, ByteArray> = EnumMap(Version::class.java)
    private val mappings: MutableMap<Version, Version> = EnumMap(Version::class.java)
    fun encode() {
        val hashes: MutableMap<Int, Version> = HashMap()
        for (version in Version.values()) {
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

    override fun encode(packet: ByteMessage, version: Version?) {
        val mapped = mappings[version]
        val message = versionMessages[mapped]
        if (message != null) packet.writeBytes(message) else throw IllegalArgumentException("No mappings for version $version")
    }

    companion object {
        fun of(packet: LimboS2CPacket): PacketSnapshot {
            val snapshot = PacketSnapshot(packet)
            snapshot.encode()
            return snapshot
        }
    }
}
