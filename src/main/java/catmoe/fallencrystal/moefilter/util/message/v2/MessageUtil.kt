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

package catmoe.fallencrystal.moefilter.util.message.v2

import catmoe.fallencrystal.moefilter.MoeFilter
import catmoe.fallencrystal.moefilter.network.bungee.util.bconnection.ConnectionUtil
import catmoe.fallencrystal.moefilter.util.message.component.ComponentUtil
import catmoe.fallencrystal.moefilter.util.message.v2.packet.MessagePacket
import catmoe.fallencrystal.moefilter.util.message.v2.packet.ViaActionbarPacket
import catmoe.fallencrystal.moefilter.util.message.v2.packet.ViaChatPacket
import catmoe.fallencrystal.moefilter.util.message.v2.packet.type.MessagesType
import catmoe.fallencrystal.moefilter.util.message.v2.packet.type.MessagesType.ACTION_BAR
import catmoe.fallencrystal.moefilter.util.message.v2.packet.type.MessagesType.CHAT
import catmoe.fallencrystal.moefilter.util.plugin.util.Scheduler
import com.github.benmanes.caffeine.cache.Caffeine
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.chat.ComponentSerializer
import net.md_5.bungee.protocol.ProtocolConstants
import net.md_5.bungee.protocol.packet.Chat
import net.md_5.bungee.protocol.packet.SystemChat
import net.md_5.bungee.protocol.packet.Title
import java.util.concurrent.TimeUnit
import java.util.logging.Level

@Suppress("unused")
object MessageUtil {
    // Type(Enum's Field) + Message, Packet
    private val packetCache = Caffeine.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).build<String, MessagePacket>()
    private val bungee = ProxyServer.getInstance()
    private val logger = bungee.logger
    private val scheduler = Scheduler(MoeFilter.instance)

    private fun packetBuilder(message: String, type: MessagesType, protocol: List<Int>) : MessagePacket {
        when (type) {
            ACTION_BAR -> {
                val actionbar = ChatMessageType.ACTION_BAR.ordinal
                val cached = packetCache.getIfPresent(("${ACTION_BAR.prefix}$message")) as ViaActionbarPacket?
                val bc = if (cached?.bc != null) cached.bc else colorize(message)
                val componentSerializer = if (cached?.cs != null) cached.cs else ComponentSerializer.toString(bc)
                var need119 = false
                var need117 = false
                var need111 = false
                var need110 = false
                protocol.forEach {
                    if (it >= ProtocolConstants.MINECRAFT_1_19) { need119=true } else if (it > ProtocolConstants.MINECRAFT_1_17) { need117=true }
                    else if (it > ProtocolConstants.MINECRAFT_1_10) { need111=true } else { need110=true }
                }
                val p119 = if (cached?.has119Data == true) cached.v119 else if (need119) SystemChat(componentSerializer, actionbar) else null
                val p117 = if (cached?.has117Data == true) cached.v117 else if (need117) Chat(componentSerializer, actionbar.toByte(), null) else null
                val p111 = if (cached?.has111Data == true) cached.v111 else if (need111) { val t = Title(); t.action=Title.Action.ACTIONBAR; t.text=componentSerializer; t  } else null
                val p110 = if (cached?.has110Data == true) cached.v110 else if (need110) Chat(ComponentSerializer.toString(TextComponent(BaseComponent.toLegacyText(bc))), actionbar.toByte(), null) else null
                val packet = ViaActionbarPacket(p119, p117, p111, p110, p119 != null, p117 != null, p111 != null, p110 != null, bc, componentSerializer , message)
                packetCache.put("${ACTION_BAR.prefix}$message", packet)
                return packet
            }
            CHAT -> {
                val cached = packetCache.getIfPresent("${CHAT.prefix}$message") as ViaChatPacket?
                val bc = if (cached?.bc != null) cached.bc else colorize(message)
                val componentSerializer = if (cached?.cs != null) cached.cs else ComponentSerializer.toString(bc)
                var need119 = false
                var needLegacy = false
                protocol.forEach { if (it >= ProtocolConstants.MINECRAFT_1_19) need119=true else needLegacy=true }
                val p119 = if (cached?.has119Data == true) cached.v119 else if (need119) SystemChat(componentSerializer, ChatMessageType.SYSTEM.ordinal) else null
                val legacy = if (cached?.hasLegacyData == true) cached.legacy else if (needLegacy) Chat(componentSerializer, ChatMessageType.CHAT.ordinal.toByte(), null) else null
                val packet = ViaChatPacket(p119, legacy, p119 != null, legacy != null, bc, componentSerializer, message)
                packetCache.put("${CHAT.prefix}$message", packet)
                return packet
            }
        }
    }

    fun sendMessage(message: String, type: MessagesType, connection: ConnectionUtil) {
        scheduler.runAsync {
            var packet = packetCache.getIfPresent("${type.prefix}$message") ?: packetBuilder(message, type, listOf(connection.getVersion()))
            if (!packet.supportChecker(connection.getVersion())) packet = packetBuilder(message, type, listOf(connection.getVersion()))
            packetSender(packet, connection)
        }
    }

    private fun packetSender(p: MessagePacket, connection: ConnectionUtil) {
        if (p.getOriginal().isEmpty()) return
        when (p.getType()) {
            ACTION_BAR -> {
                val packet = p as ViaActionbarPacket
                val version = connection.getVersion()
                if (version >= ProtocolConstants.MINECRAFT_1_19) { connection.writePacket(packet.v119!!); return }
                if (version > ProtocolConstants.MINECRAFT_1_17) { connection.writePacket(packet.v117!!); return }
                if (version > ProtocolConstants.MINECRAFT_1_10) { connection.writePacket(packet.v111!!); return }
                if (version >= ProtocolConstants.MINECRAFT_1_8) { connection.writePacket(packet.v110!!); return }
                throw IllegalStateException("Need send protocol $version but not available packets for this version.")
            }
            CHAT -> {
                val packet = p as ViaChatPacket
                if (connection.getVersion() >= ProtocolConstants.MINECRAFT_1_19) connection.writePacket(packet.v119!!)
                else if (connection.getVersion() >= ProtocolConstants.MINECRAFT_1_8) connection.writePacket(packet.legacy!!)
                else throw IllegalStateException("Need send protocol ${connection.getVersion()} but not available packets for this version.")
            }
        }
    }

    fun sendMessage(message: String, type: MessagesType, sender: ProxiedPlayer) {
        val connection = ConnectionUtil(sender.pendingConnection)
        val packet =  packetCache.getIfPresent("${type.prefix}$message") ?: packetBuilder(message, type, listOf(connection.getVersion()))
        packetSender(packet, connection)
    }

    fun sendMessage(message: String, type: MessagesType, sender: CommandSender) {
        val version = if (sender is ProxiedPlayer) sender.pendingConnection.version else 0
        val packet = packetCache.getIfPresent("${type.prefix}$message") ?: packetBuilder(message, type, listOf(version))
        if (sender is ProxiedPlayer) {
            val connection = ConnectionUtil(sender.pendingConnection)
            packetSender(packet, connection)
        } else { logInfo(packet.getOriginal()) }
    }

    fun invalidateCache(type: MessagesType, message: String) { packetCache.invalidate("${type.prefix}$message") }

    fun argsBuilder(startIndex: Int, args: Array<out String>?): StringBuilder {
        val message = StringBuilder()
        if (args != null) { for (i in startIndex until args.size) { message.append(args[i]).append(" ") } }
        return message
    }

    fun logInfo(message: String) { logger.log(Level.INFO, logColorize(message)) }

    fun logWarn(message: String) { logger.log(Level.WARNING, logColorize(message)) }

    fun logError(message: String) { logger.log(Level.SEVERE, logColorize(message)) }

    fun colorize(message: String): BaseComponent { return ComponentUtil.toBaseComponents(ComponentUtil.parse(message)) }

    private fun logColorize(message: String): String {
        return if (message.contains(">") && message.contains(">")) colorize(message).toLegacyText() else message
    }
}
