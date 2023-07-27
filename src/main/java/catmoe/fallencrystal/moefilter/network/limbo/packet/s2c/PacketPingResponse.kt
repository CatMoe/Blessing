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

import catmoe.fallencrystal.moefilter.network.limbo.packet.ByteMessage
import catmoe.fallencrystal.moefilter.network.limbo.packet.LimboS2CPacket
import catmoe.fallencrystal.moefilter.network.limbo.util.Version

class PacketPingResponse : LimboS2CPacket() {
    override fun encode(packet: ByteMessage, version: Version?) {
        val protocol = version!!.protocolNumber
        val brand = "MoeLimbo"
        val description = "{\"text\": \"§dMoeLimbo\"}"
        // Protocol (ServerBrand, Protocol), PlayerInfo (1, 0, List<UUID,String>), Description
        val output =
            "{ \"version\": { \"name\": \"$brand\", \"protocol\": $protocol }, " +
                    "\"players\": { \"max\": ${1}, \"online\": ${0}, \"sample\": [] }, " +
                    "\"description\": $description }"
        packet.writeString(output)
    }
}