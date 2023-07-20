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
import catmoe.fallencrystal.moefilter.util.message.v2.packet.type.MessagesType
import catmoe.fallencrystal.moefilter.util.message.v2.packet.type.MessagesType.ACTION_BAR
import catmoe.fallencrystal.moefilter.util.message.v2.packet.type.MessagesType.CHAT
import catmoe.fallencrystal.moefilter.util.message.v2.processor.actionbar.ActionbarPacketProcessor
import catmoe.fallencrystal.moefilter.util.message.v2.processor.cache.MessagePacketCache
import catmoe.fallencrystal.moefilter.util.message.v2.processor.chat.ChatPacketProcessor
import catmoe.fallencrystal.moefilter.util.plugin.util.Scheduler
import com.github.benmanes.caffeine.cache.Caffeine
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.connection.ProxiedPlayer
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
        return when (type) {
            ACTION_BAR -> { ActionbarPacketProcessor().process(message, protocol) }
            CHAT -> { ChatPacketProcessor().process(message, protocol) }
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
            ACTION_BAR -> { ActionbarPacketProcessor().send(p, connection) }
            CHAT -> { ChatPacketProcessor().send(p, connection) }
        }
    }

    fun sendMessage(message: String, type: MessagesType, sender: ProxiedPlayer) {
        val connection = ConnectionUtil(sender.pendingConnection)
        val packet =  MessagePacketCache.readPacket(type.processor, message) ?: packetBuilder(message, type, listOf(connection.getVersion()))
        packetSender(packet, connection)
    }

    fun sendMessage(message: String, type: MessagesType, sender: CommandSender) {
        val version = if (sender is ProxiedPlayer) sender.pendingConnection.version else 0
        val packet = MessagePacketCache.readPacket(type.processor, message) ?: packetBuilder(message, type, listOf(version))
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
