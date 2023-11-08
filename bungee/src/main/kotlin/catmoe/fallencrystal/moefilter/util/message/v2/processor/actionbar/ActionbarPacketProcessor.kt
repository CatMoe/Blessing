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

package catmoe.fallencrystal.moefilter.util.message.v2.processor.actionbar

import catmoe.fallencrystal.moefilter.network.bungee.util.bconnection.ConnectionUtil
import catmoe.fallencrystal.moefilter.util.message.v2.packet.MessageActionbarPacket
import catmoe.fallencrystal.moefilter.util.message.v2.packet.MessagePacket
import catmoe.fallencrystal.moefilter.util.message.v2.packet.type.MessagesType
import catmoe.fallencrystal.moefilter.util.message.v2.processor.AbstractMessageProcessor
import catmoe.fallencrystal.moefilter.util.message.v2.processor.PacketMessageType
import catmoe.fallencrystal.moefilter.util.message.v2.processor.cache.MessagePacketCache
import catmoe.fallencrystal.moefilter.util.plugin.AsyncLoader
import catmoe.fallencrystal.translation.utils.version.Version
import catmoe.fallencrystal.translation.utils.version.Version.*
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.chat.ComponentSerializer
import net.md_5.bungee.protocol.packet.Chat
import net.md_5.bungee.protocol.packet.SystemChat
import net.md_5.bungee.protocol.packet.Title
import java.lang.reflect.Method

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
        val p116 = cached?.v116 ?: /* get116(serializer, need116) */ createTitleText(serializer, need116)
        val p111 = cached?.v111 ?: /* get111(legacySerializer, need111) */ createTitleText(legacySerializer, need111)
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
        if (version.moreOrEqual(V1_19)) { connection.writePacket(p.v119!!); return }
        if (version.moreOrEqual(V1_17)) { connection.writePacket(p.v117!!); return }
        if (version.moreOrEqual(V1_16)) { connection.writePacket(p.v116!!); return }
        if (version.more(V1_10)) { connection.writePacket(p.v111!!); return }
        if (version.moreOrEqual(V1_7_6)) { connection.writePacket(p.v110!!); return }
        throw IllegalStateException("Need send protocol ${version.number} but not available packets for this version.")
    }

    private fun get119(serializer: String, need: Boolean): SystemChat? {
        return if (need) when (AsyncLoader.isLegacy) {
            true -> c1!!.newInstance(serializer, aOrdinal)
            false -> SystemChat(ComponentSerializer.deserialize(serializer), aOrdinal)
        } else null
    }

    private fun get117(serializer: String, need: Boolean): Chat? { return if (need) Chat(serializer, aOrdinal.toByte(), null) else null }

    private fun createTitleText(serializer: String, need: Boolean): Title? {
        if (!need) return null
        val title = Title(Title.Action.ACTIONBAR)
        when (AsyncLoader.isLegacy) {
            true -> setTitleMethod!!.invoke(title, serializer)
            false -> title.text=ComponentSerializer.deserialize(serializer)
        }
        return title
    }

    private fun get110(component: BaseComponent, need: Boolean): Chat? {
        return if (need) Chat(ComponentSerializer.toString(TextComponent(BaseComponent.toLegacyText(component))), aOrdinal.toByte(), null) else null
    }

    companion object {
        //private val setTitleMethod: Method = Title::class.java.getMethod("setText", String::class.java)
        private val c1 = try { SystemChat::class.java.getConstructor(String::class.java, Int::class.java) } catch (_: NoSuchMethodException) { null }
        private val setTitleMethod: Method? = try { Title::class.java.getMethod("setText", String::class.java) } catch (_: NoSuchMethodException) { null }
    }
}