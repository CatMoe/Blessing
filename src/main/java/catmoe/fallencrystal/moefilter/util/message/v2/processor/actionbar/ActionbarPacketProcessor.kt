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

package catmoe.fallencrystal.moefilter.util.message.v2.processor.actionbar

import catmoe.fallencrystal.moefilter.network.bungee.util.bconnection.ConnectionUtil
import catmoe.fallencrystal.moefilter.util.message.v2.packet.MessageActionbarPacket
import catmoe.fallencrystal.moefilter.util.message.v2.packet.MessagePacket
import catmoe.fallencrystal.moefilter.util.message.v2.packet.type.MessagesType
import catmoe.fallencrystal.moefilter.util.message.v2.processor.AbstractMessageProcessor
import catmoe.fallencrystal.moefilter.util.message.v2.processor.PacketMessageType
import catmoe.fallencrystal.moefilter.util.message.v2.processor.cache.MessagePacketCache
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.protocol.ProtocolConstants
import net.md_5.bungee.protocol.packet.Chat
import net.md_5.bungee.protocol.packet.SystemChat
import net.md_5.bungee.protocol.packet.Title

@PacketMessageType(MessagesType.ACTION_BAR)
class ActionbarPacketProcessor : AbstractMessageProcessor() {

    val aOrdinal = ChatMessageType.ACTION_BAR.ordinal

    override fun process(message: String, protocol: List<Int>): MessagePacket {
        val cached = MessagePacketCache.readPacket(this, message) as? MessageActionbarPacket
        val baseComponent = getBaseComponent(cached, message)
        val serializer = getSerializer(cached, baseComponent)
        var need119 = cached?.has119Data ?: false
        var need117 = cached?.has117Data ?: false
        var need111 = cached?.has111Data ?: false
        var need110 = cached?.has110Data ?: false
        protocol.forEach {
            if (it >= ProtocolConstants.MINECRAFT_1_19) { need119=true } else if (it > ProtocolConstants.MINECRAFT_1_17) { need117=true }
            else if (it > ProtocolConstants.MINECRAFT_1_10) { need111=true } else { need110=true }
        }
        val p119 = if (cached?.v119 != null) cached.v119 else get119(serializer, need119)
        val p117 = if (cached?.v117 != null) cached.v117 else get117(serializer, need117)
        val p111 = if (cached?.v111 != null) cached.v111 else get111(serializer, need111)
        val p110 = if (cached?.v110 != null) cached.v110 else get110(baseComponent, need110)
        val packet = MessageActionbarPacket(
            p119, p117, p111, p110,
            need119, need117, need111, need110,
            baseComponent, serializer, message
        )
        MessagePacketCache.writePacket(this, packet)
        return packet
    }

    override fun send(packet: MessagePacket, connection: ConnectionUtil) {
        var p = packet as MessageActionbarPacket
        val version = connection.getVersion()
        if (!p.supportChecker(version)) p = process(p.originalMessage, listOf(version)) as MessageActionbarPacket
        if (version >= ProtocolConstants.MINECRAFT_1_19) { connection.writePacket(p.v119!!); return }
        if (version > ProtocolConstants.MINECRAFT_1_17) { connection.writePacket(p.v117!!); return }
        if (version > ProtocolConstants.MINECRAFT_1_10) { connection.writePacket(p.v111!!); return }
        if (version >= ProtocolConstants.MINECRAFT_1_8) { connection.writePacket(p.v110!!); return }
        throw IllegalStateException("Need send protocol $version but not available packets for this version.")
    }

    private fun get119(serializer: String, need: Boolean): SystemChat? { return if (need) SystemChat(serializer, aOrdinal) else null }

    private fun get117(serializer: String, need: Boolean): Chat? { return if (need) Chat(serializer, aOrdinal.toByte(), null) else null }

    private fun get111(serializer: String, need: Boolean): Title? {
        if (!need) return null
        val title = Title()
        title.action=Title.Action.ACTIONBAR
        title.text=serializer
        return title
    }

    private fun get110(baseComponent: BaseComponent, need: Boolean): Chat? {
        return if (need) Chat(getSerializer(null, TextComponent(BaseComponent.toLegacyText(baseComponent))), aOrdinal.toByte(), null) else null
    }
}