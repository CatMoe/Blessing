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

package catmoe.fallencrystal.moefilter.util.message.v2.processor.chat

import catmoe.fallencrystal.moefilter.network.bungee.util.bconnection.ConnectionUtil
import catmoe.fallencrystal.moefilter.util.message.v2.packet.MessageChatPacket
import catmoe.fallencrystal.moefilter.util.message.v2.packet.MessagePacket
import catmoe.fallencrystal.moefilter.util.message.v2.packet.type.MessagesType
import catmoe.fallencrystal.moefilter.util.message.v2.processor.AbstractMessageProcessor
import catmoe.fallencrystal.moefilter.util.message.v2.processor.PacketMessageType
import catmoe.fallencrystal.moefilter.util.message.v2.processor.cache.MessagePacketCache
import catmoe.fallencrystal.translation.utils.version.Version
import catmoe.fallencrystal.translation.utils.version.Version.V1_16
import catmoe.fallencrystal.translation.utils.version.Version.V1_19
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.chat.ComponentSerializer
import net.md_5.bungee.protocol.packet.Chat
import net.md_5.bungee.protocol.packet.SystemChat

@PacketMessageType(MessagesType.CHAT)
class ChatPacketProcessor : AbstractMessageProcessor() {
    override fun process(message: String, protocol: List<Int>): MessagePacket {
        /*
        笔记: 即使as ViaChatPacket没有显式可空. cached的赋值实际上也是ViaChatPacket?
        如果转换失败 cached不会抛出ClassCastException 而是返回null
         */
        val cached = MessagePacketCache(this).readCachedAndWrite(message) as? MessageChatPacket
        // getBaseComponent & getSerializer 方法都在抽象类中实践.
        val component = getComponent(cached, message)
        val legacyComponent = getLegacyComponent(cached, message)
        val serializer = getSerializer(cached, component)
        val legacySerializer = getLegacySerializer(cached, legacyComponent)
        // 1.19+ 应该使用SystemChat而不是Chat 需要区分这一点.
        var need119 = cached?.v119 != null
        var needLegacy = cached?.legacy != null
        var needLegacy2 = cached?.legacy2 != null
        protocol.forEach {
            val v = Version.of(it)
            when {
                v.moreOrEqual(V1_19) -> need119=true
                v.moreOrEqual(V1_16) -> needLegacy=true
                else -> needLegacy2=true
            }
        }
        val p119 = cached?.v119 ?: get119(serializer, need119)
        val legacy = cached?.legacy ?: getLegacy(serializer, needLegacy)
        val legacy2 = cached?.legacy2 ?: getLegacy(legacySerializer, needLegacy2)
        val packet = MessageChatPacket(p119, legacy, legacy2, component, serializer, legacyComponent, serializer, message)
        MessagePacketCache(this).writeCache(packet)
        return packet
    }

    override fun send(packet: MessagePacket, connection: ConnectionUtil) {
        var p = packet as MessageChatPacket
        if (!p.supportChecker(connection.version)) p = process(p.originalMessage, listOf(connection.version)) as MessageChatPacket
        val v = Version.of(connection.version)
        when {
            v.moreOrEqual(V1_19) -> connection.writePacket(p.v119!!)
            v.moreOrEqual(V1_16) -> connection.writePacket(p.legacy!!)
            else -> connection.writePacket(p.legacy2!!)
        }
    }

    private fun get119(serializer: String, need: Boolean): SystemChat? { return if (need) SystemChat(ComponentSerializer.deserialize(serializer), ChatMessageType.SYSTEM.ordinal) else null }

    private fun getLegacy(serializer: String, need: Boolean): Chat? { return if (need) Chat(serializer, ChatMessageType.CHAT.ordinal.toByte(), null) else null }
}