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
import catmoe.fallencrystal.moefilter.network.limbo.util.LimboLocation
import catmoe.fallencrystal.moefilter.network.limbo.util.Version
import io.netty.channel.Channel

@Suppress("MemberVisibilityCanBePrivate")
class PacketClientPosition : LimboC2SPacket() {

    var lastLoc: LimboLocation? = null

    override fun decode(packet: ByteMessage, channel: Channel, version: Version?) {
        lastLoc = LimboLocation(
            packet.readDouble(), // x
            packet.readDouble(), // y
            packet.readDouble(), // z
            0f, 0f, // PlayerPosition don't have any method to read yaw & pitch.
            packet.readBoolean() // onGround
        )
    }

    override fun handle(handler: LimboHandler) {
        val loc = handler.location
        val lastLoc = lastLoc ?: return
        handler.location = if (loc == null) lastLoc
        else LimboLocation(
            lastLoc.x, lastLoc.y, lastLoc.z,
            loc.yaw, loc.pitch,
            lastLoc.onGround
        )
    }

    override fun toString(): String { return "PacketClientPosition(location=$lastLoc)" }
}