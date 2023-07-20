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

package catmoe.fallencrystal.moefilter.util.message.v2.processor.chat

import catmoe.fallencrystal.moefilter.network.bungee.util.bconnection.ConnectionUtil
import catmoe.fallencrystal.moefilter.util.message.v2.packet.MessageChatPacket
import catmoe.fallencrystal.moefilter.util.message.v2.packet.MessagePacket
import catmoe.fallencrystal.moefilter.util.message.v2.packet.type.MessagesType
import catmoe.fallencrystal.moefilter.util.message.v2.processor.AbstractMessageProcessor
import catmoe.fallencrystal.moefilter.util.message.v2.processor.PacketMessageType
import catmoe.fallencrystal.moefilter.util.message.v2.processor.cache.MessagePacketCache
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.protocol.ProtocolConstants
import net.md_5.bungee.protocol.packet.Chat
import net.md_5.bungee.protocol.packet.SystemChat

@PacketMessageType(MessagesType.CHAT)
class ChatPacketProcessor : AbstractMessageProcessor() {
    override fun process(message: String, protocol: List<Int>): MessagePacket {
        /*
        笔记: 即使as ViaChatPacket没有显式可空. cached的赋值实际上也是ViaChatPacket?
        如果转换失败 cached不会抛出ClassCastException 而是返回null
         */
        val cached = MessagePacketCache.readPacket(this, message) as? MessageChatPacket
        // getBaseComponent & getSerializer 方法都在抽象类中实践.
        val baseComponent = getBaseComponent(cached, message)
        val serializer = getSerializer(cached, baseComponent)
        // 1.19+ 应该使用SystemChat而不是Chat 需要区分这一点.
        var need119 = cached?.has119Data ?: false; var needLegacy = cached?.hasLegacyData ?: false
        protocol.forEach { if (it >= ProtocolConstants.MINECRAFT_1_19) need119=true else needLegacy=true }
        val p119 = if (cached?.has119Data == true) cached.v119 else get119(serializer, need119)
        val legacy = if (cached?.hasLegacyData == true) cached.legacy else getLegacy(serializer, needLegacy)
        val packet = MessageChatPacket(p119, legacy, need119, needLegacy, baseComponent, serializer, message)
        MessagePacketCache.writePacket(this, packet)
        return packet
    }

    override fun send(packet: MessagePacket, connection: ConnectionUtil) {
        var p = packet as MessageChatPacket
        if (!p.supportChecker(connection.getVersion())) p = process(p.originalMessage, listOf(connection.getVersion())) as MessageChatPacket
        if (connection.getVersion() >= ProtocolConstants.MINECRAFT_1_19) connection.writePacket(p.v119!!)
        else if (connection.getVersion() >= ProtocolConstants.MINECRAFT_1_8) connection.writePacket(p.legacy!!)
        else throw IllegalStateException("Need send protocol ${connection.getVersion()} but not available packets for this version.")
    }

    private fun get119(serializer: String, need: Boolean): SystemChat? { return if (need) SystemChat(serializer, ChatMessageType.SYSTEM.ordinal) else null }

    private fun getLegacy(serializer: String, need: Boolean): Chat? { return if (need) Chat(serializer, ChatMessageType.CHAT.ordinal.toByte(), null) else null }
}