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
import catmoe.fallencrystal.moefilter.network.limbo.util.Version
import catmoe.fallencrystal.moefilter.network.limbo.util.Version.*
import catmoe.fallencrystal.moefilter.util.message.v2.packet.MessageActionbarPacket
import catmoe.fallencrystal.moefilter.util.message.v2.packet.MessagePacket
import catmoe.fallencrystal.moefilter.util.message.v2.packet.type.MessagesType
import catmoe.fallencrystal.moefilter.util.message.v2.processor.AbstractMessageProcessor
import catmoe.fallencrystal.moefilter.util.message.v2.processor.PacketMessageType
import catmoe.fallencrystal.moefilter.util.message.v2.processor.cache.MessagePacketCache
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.protocol.packet.Chat
import net.md_5.bungee.protocol.packet.SystemChat
import net.md_5.bungee.protocol.packet.Title

@PacketMessageType(MessagesType.ACTION_BAR)
class ActionbarPacketProcessor : AbstractMessageProcessor() {

    private val aOrdinal = ChatMessageType.ACTION_BAR.ordinal

    override fun process(message: String, protocol: List<Int>): MessagePacket {
        val cached = MessagePacketCache.readPacket(this, message) as? MessageActionbarPacket
        val baseComponent = getBaseComponent(cached, message)
        val legacyComponent = getLegacyComponent(cached, message)
        val serializer = getSerializer(cached, baseComponent)
        val legacySerializer = getLegacySerializer(cached, legacyComponent)
        var need119 = cached?.has119Data ?: false
        var need117 = cached?.has117Data ?: false
        var need116 = cached?.has116Data ?: false
        var need111 = cached?.has111Data ?: false
        var need110 = cached?.has110Data ?: false
        protocol.forEach {
            /*
            if (it >= ProtocolConstants.MINECRAFT_1_19) need119=true else if (it > ProtocolConstants.MINECRAFT_1_17) need117=true
            else if (it > ProtocolConstants.MINECRAFT_1_16) need116=true
            else if (it > ProtocolConstants.MINECRAFT_1_10) need111=true else need110=true
             */
            if (it >= V1_19.number) need119=true else if (it > V1_17.number) need117=true
            else if (it > V1_16.number) need116=true else if (it > V1_10.number) need111=true
            else need110 = true
        }
        val p119 = if (cached?.v119 != null) cached.v119 else get119(serializer, need119)
        val p117 = if (cached?.v117 != null) cached.v117 else get117(serializer, need117)
        val p116 = cached?.v116 ?: get116(legacySerializer, need116)
        val p111 = if (cached?.v111 != null) cached.v111 else get111(legacySerializer, need111)
        val p110 = if (cached?.v110 != null) cached.v110 else get110(legacyComponent, need110)
        val packet = MessageActionbarPacket(
            p119, p117, p116, p111, p110,
            need119, need117, need116, need111, need110,
            baseComponent, serializer, legacyComponent, legacySerializer, message
        )
        MessagePacketCache.writePacket(this, packet)
        return packet
    }

    override fun send(packet: MessagePacket, connection: ConnectionUtil) {
        var p = packet as MessageActionbarPacket
        val version = Version.of(connection.version)
        if (!p.supportChecker(version.number)) p = process(p.originalMessage, listOf(version.number)) as MessageActionbarPacket
        /*
        if (version >= ProtocolConstants.MINECRAFT_1_19) { connection.writePacket(p.v119!!); return }
        if (version > ProtocolConstants.MINECRAFT_1_17) { connection.writePacket(p.v117!!); return }
        if (version >= ProtocolConstants.MINECRAFT_1_16) { connection.writePacket(p.v116!!); return }
        if (version > ProtocolConstants.MINECRAFT_1_10) { connection.writePacket(p.v111!!); return }
        if (version >= ProtocolConstants.MINECRAFT_1_8) { connection.writePacket(p.v110!!); return }
        throw IllegalStateException("Need send protocol $version but not available packets for this version.")
         */
        if (version.moreOrEqual(V1_19)) { connection.writePacket(p.v119!!); return }
        if (version.moreOrEqual(V1_17)) { connection.writePacket(p.v117!!); return }
        if (version.moreOrEqual(V1_16)) { connection.writePacket(p.v116!!); return }
        if (version.more(V1_10)) { connection.writePacket(p.v111!!); return }
        if (version.moreOrEqual(V1_7_6)) { connection.writePacket(p.v110!!); return }
        throw IllegalStateException("Need send protocol ${version.number} but not available packets for this version.")
    }

    private fun get119(serializer: String, need: Boolean): SystemChat? { return if (need) SystemChat(serializer, aOrdinal) else null }

    private fun get117(serializer: String, need: Boolean): Chat? { return if (need) Chat(serializer, aOrdinal.toByte(), null) else null }

    private fun get116(serializer: String, need: Boolean): Title? {
        if (!need) return null
        val t = Title()
        t.action=Title.Action.ACTIONBAR
        t.text=serializer
        return t
    }

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