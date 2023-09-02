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

package catmoe.fallencrystal.moefilter.network.limbo.packet.common

import catmoe.fallencrystal.moefilter.network.limbo.netty.ByteMessage
import catmoe.fallencrystal.moefilter.network.limbo.packet.LimboPacket
import catmoe.fallencrystal.translation.utils.version.Version
import io.netty.channel.Channel

@Suppress("MemberVisibilityCanBePrivate")
class TabComplete : LimboPacket {

    /* Common */
    var transactionId = -1

    /* Request */
    var cursor = ""
    var hasPosition = false
    var position = 0L

    override fun decode(packet: ByteMessage, channel: Channel, version: Version?) {
        if (version!!.moreOrEqual(Version.V1_13)) transactionId=packet.readVarInt()
        cursor = packet.readString(length = 32767)
        if (version.less(Version.V1_13)) {
            if (version.moreOrEqual(Version.V1_9)) packet.readBoolean()
            this.hasPosition = packet.readBoolean()
            if (hasPosition) position=packet.readLong()
        }
    }

    /* Response */
    val commands: MutableCollection<String> = ArrayList()

    var rangeStart = if (position != 0L) position.toInt() else 1
    var rangeLength = rangeStart+1


    override fun encode(packet: ByteMessage, version: Version?) {
        if (version!!.moreOrEqual(Version.V1_13)) {
            packet.writeVarInt(transactionId)
            packet.writeVarInt(rangeStart)
            packet.writeVarInt(rangeLength)
            for (i in commands) {
                packet.writeString(i)

                // TODO: 1.13+ suggestions tip
                packet.writeBoolean(false)
            }
        } else packet.writeStringsArray(commands.toTypedArray())
    }
}