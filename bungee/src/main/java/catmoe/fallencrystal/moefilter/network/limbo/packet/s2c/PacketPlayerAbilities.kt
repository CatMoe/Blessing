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

import catmoe.fallencrystal.moefilter.network.common.ByteMessage
import catmoe.fallencrystal.moefilter.network.limbo.packet.LimboS2CPacket
import catmoe.fallencrystal.translation.utils.version.Version

@Suppress("MemberVisibilityCanBePrivate")
class PacketPlayerAbilities : LimboS2CPacket() {

    var flags: Int? = null
    var flyingSpeed: Float = 0f
    var fieldOfView: Float = 0.1f

    override fun encode(packet: ByteMessage, version: Version?) {
        packet.writeByte(flags ?: 0x02)
        //listOf(flyingSpeed, fieldOfView).forEach { packet.writeFloat(it) }
        packet.writeFloat(flyingSpeed)
        packet.writeFloat(fieldOfView)
    }

    override fun toString(): String {
        return "PacketPlayerAbilities(flags=$flags, flyingSpeed=$flyingSpeed, fieldOfView=$fieldOfView)"
    }

}