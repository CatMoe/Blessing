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
import catmoe.fallencrystal.translation.utils.version.Version
import java.util.*

class PacketLoginSuccess : LimboS2CPacket() {
    var uuid: UUID? = null
    var username = ""
    override fun encode(packet: ByteMessage, version: Version?) {
        val uuid = this.uuid ?: UUID.randomUUID()
        if (version!!.moreOrEqual(Version.V1_16)) packet.writeUuid(uuid)
        else if (version.moreOrEqual(Version.V1_7_6)) packet.writeString(uuid.toString())
        else packet.writeString(uuid.toString().replace("-", ""))
        packet.writeString(username)
        if (version.moreOrEqual(Version.V1_19)) packet.writeVarInt(0)
    }

    override fun toString(): String { return "PacketLoginSuccess(uuid=$uuid, username=$username)" }
}