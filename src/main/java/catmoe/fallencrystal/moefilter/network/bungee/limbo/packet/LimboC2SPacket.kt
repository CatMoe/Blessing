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

import catmoe.fallencrystal.moefilter.network.bungee.limbo.exception.InvalidPacketOperation
import catmoe.fallencrystal.moefilter.network.bungee.limbo.handshake.Version
import io.netty.channel.Channel

abstract class LimboC2SPacket : LimboPacket {
    override fun encode(packet: ByteMessage, channel: Channel, version: Version?) {
        throw InvalidPacketOperation("encode does not work with C2S packet")
    }
}