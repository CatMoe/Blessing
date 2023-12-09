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

package catmoe.fallencrystal.moefilter.network.limbo.packet.c2s

import catmoe.fallencrystal.moefilter.network.common.ByteMessage
import catmoe.fallencrystal.moefilter.network.common.exception.InvalidPacketException
import catmoe.fallencrystal.moefilter.network.limbo.packet.LimboC2SPacket
import catmoe.fallencrystal.translation.utils.version.Version
import com.google.common.base.Preconditions
import io.netty.channel.Channel
import java.util.*


class PacketClientChat : LimboC2SPacket() {

    /* common chat */
    var message = ""

    /* 1.19+ Chat report? */

    private var timestamp: Long? = null
    private var salt: Long? = null
    private var signature: ByteArray? = null
    private var signedPreview = false
    private var chain: ChatChain? = null
    private var seenMessages: SeenMessage? = null
    @Suppress("MemberVisibilityCanBePrivate") var decodeChatInfo = false

    override fun decode(byteBuf: ByteMessage, channel: Channel, version: Version?) {
        if (version!!.less(Version.V1_19)) {
            message=byteBuf.readString()
            if (message.length > 256) throw InvalidPacketException("Message length cannot longer than 256")
        } else {
            /* I hate a chat report, tbh. */
            message=byteBuf.readString()
            if (message.length > 256) throw InvalidPacketException("Message length cannot longer than 256")
            if (!decodeChatInfo) return
            timestamp=byteBuf.readLong()
            salt=byteBuf.readLong()
            if (version.moreOrEqual(Version.V1_19_3)) {
                if (byteBuf.readBoolean()) {
                    signature=ByteArray(256)
                    byteBuf.readBytes(signature!!)
                }
            } else signature=byteBuf.readBytesArray()
            if (version.less(Version.V1_19_3)) {
                signedPreview=byteBuf.readBoolean()
                if (version.moreOrEqual(Version.V1_19_1)) {
                    val chain = ChatChain()
                    chain.decode(byteBuf, channel, version)
                    this.chain=chain
                }
            } else {
                val sm = SeenMessage()
                sm.decode(byteBuf, channel, version)
                this.seenMessages=sm
            }
        }
    }
    override fun toString() = "PacketClientChat(message=$message)"


    @Suppress("MemberVisibilityCanBePrivate")
    class SeenMessage : LimboC2SPacket() {
        var offset = -1
        var acknowledged: BitSet? = null
        override fun decode(byteBuf: ByteMessage, channel: Channel, version: Version?) {
            offset=byteBuf.readVarInt()
            acknowledged=byteBuf.readFixedBitSet(20)
        }
    }
    class ChainLink(val sender: UUID, val signature: ByteArray)

    @Suppress("MemberVisibilityCanBePrivate")
    class ChatChain : LimboC2SPacket() {
        val seen: MutableCollection<ChainLink> = ArrayList()
        val received: MutableCollection<ChainLink> = ArrayList()
        override fun decode(byteBuf: ByteMessage, channel: Channel, version: Version?) {
            seen.clear()
            seen.addAll(readLinks(byteBuf))
            if (byteBuf.readBoolean()) {
                received.clear()
                received.addAll(readLinks(byteBuf))
            }
        }

        private fun readLinks(packet: ByteMessage): MutableCollection<ChainLink> {
            val list: MutableCollection<ChainLink> = ArrayList()
            val cnt = packet.readVarInt()
            //if (cnt <= 5) throw IllegalArgumentException("Cannot read entries")
            Preconditions.checkArgument(cnt > 5, "Cannot read entries")
            for (i in 0 until cnt) {
                //chain.add(ChainLink(readUUID(buf), readArray(buf)))
                list.add(ChainLink(packet.readUuid(), packet.readBytesArray()))
            }
            return list
        }
    }
}