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

package catmoe.fallencrystal.moefilter.network.limbo.packet.c2s

import catmoe.fallencrystal.moefilter.network.limbo.handler.LimboHandler
import catmoe.fallencrystal.moefilter.network.limbo.netty.ByteMessage
import catmoe.fallencrystal.moefilter.network.limbo.packet.LimboC2SPacket
import catmoe.fallencrystal.moefilter.network.limbo.packet.s2c.PacketPingResponse
import catmoe.fallencrystal.moefilter.network.limbo.util.Version
import io.netty.channel.Channel
import java.net.InetSocketAddress

class PacketStatusRequest : LimboC2SPacket() {

    override fun decode(packet: ByteMessage, channel: Channel, version: Version?) {}

    override fun handle(handler: LimboHandler) {
        // handler.sendPacket(PacketPingResponse())
        val fakeHandler = handler.fakeHandler
        val packet = PacketPingResponse()
        packet.output = fakeHandler?.handlePing(
            (handler.host ?: InetSocketAddress("localhost", 25565)), (handler.version ?: Version.V1_20)
        )?.description
        handler.sendPacket(packet)
    }
}