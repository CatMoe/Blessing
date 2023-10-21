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
import catmoe.fallencrystal.translation.utils.version.Version
import catmoe.fallencrystal.translation.utils.version.Version.*
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.chat.ComponentSerializer
import net.md_5.bungee.protocol.packet.Chat
import net.md_5.bungee.protocol.packet.SystemChat
import net.md_5.bungee.protocol.packet.Title

@PacketMessageType(MessagesType.ACTION_BAR)
class ActionbarPacketProcessor : AbstractMessageProcessor() {

    private val aOrdinal = ChatMessageType.ACTION_BAR.ordinal

    override fun process(message: String, protocol: List<Int>): MessagePacket {
        val cached = MessagePacketCache(this).readCachedAndWrite(message) as? MessageActionbarPacket
        val baseComponent = getComponent(cached, message)
        val legacyComponent = getLegacyComponent(cached, message)
        val serializer = getSerializer(cached, baseComponent)
        val legacySerializer = getLegacySerializer(cached, legacyComponent)
        var need119 = cached?.v119 != null
        var need117 = cached?.v117 != null
        var need116 = cached?.v116 != null
        var need111 = cached?.v111 != null
        var need110 = cached?.v110 != null
        protocol.forEach {
            val version = Version.of(it)
            when {
                version.moreOrEqual(V1_19) -> need119=true
                version.more(V1_17) -> need117=true
                version.more(V1_16) -> need116=true
                version.more(V1_10) -> need111=true
                else -> need110=true
            }
        }
        val p119 = if (cached?.v119 != null) cached.v119 else get119(serializer, need119)
        val p117 = cached?.v117 ?: get117(serializer, need117)
        val p116 = cached?.v116 ?: get116(serializer, need116)
        val p111 = cached?.v111 ?: get111(legacySerializer, need111)
        val p110 = cached?.v110 ?: get110(legacyComponent, need110)
        val packet = MessageActionbarPacket(
            p119, p117, p116, p111, p110,
            baseComponent, serializer, legacyComponent, legacySerializer, message
        )
        MessagePacketCache(this).writeCache(packet)
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

    private fun get110(component: BaseComponent, need: Boolean): Chat? {
        return if (need) Chat(ComponentSerializer.toString(TextComponent(BaseComponent.toLegacyText(component))), aOrdinal.toByte(), null) else null
    }
}