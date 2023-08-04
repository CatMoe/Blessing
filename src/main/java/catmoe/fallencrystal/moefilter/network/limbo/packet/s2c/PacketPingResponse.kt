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

package catmoe.fallencrystal.moefilter.network.limbo.packet.s2c

import catmoe.fallencrystal.moefilter.network.limbo.netty.ByteMessage
import catmoe.fallencrystal.moefilter.network.limbo.packet.LimboS2CPacket
import catmoe.fallencrystal.moefilter.network.limbo.util.Version

@Suppress("MemberVisibilityCanBePrivate")
class PacketPingResponse : LimboS2CPacket() {

    var protocol = Version.UNDEFINED
    var brand = "MoeLimbo"
    var description = "{\"text\": \"§dMoeLimbo\"}"
    var max = 1
    var online = 0
    // Protocol (ServerBrand, Protocol), PlayerInfo (1, 0, List<UUID,String>), Description
    var output: String? = null
    private val template = "{ \"version\": { \"name\": \"[brand]\", \"protocol\": [protocol] }, " +
            "\"players\": { \"max\": [max], \"online\": [online], \"sample\": [] }, " +
            "\"description\": [description] }"
    var bm: ByteArray? = null

    override fun encode(packet: ByteMessage, version: Version?) {
        if (bm != null) { packet.writeBytes(bm!!); return }
        val protocol = if (this.protocol == Version.UNDEFINED) version!! else this.protocol
        val output = if (output != null && output != "") output else {
            this.template
                .replace("[brand]", brand)
                .replace("[protocol]", protocol.protocolNumber.toString())
                .replace("[max]", max.toString())
                .replace("[online]", online.toString())
                .replace("[description]", description)
        }
        packet.writeString(output)
    }

    override fun toString(): String {
        return "PacketPingResponse($output)"
    }


}