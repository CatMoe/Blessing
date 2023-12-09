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

package catmoe.fallencrystal.moefilter.util.message.v2

import catmoe.fallencrystal.moefilter.network.bungee.util.ConnectionUtil
import catmoe.fallencrystal.moefilter.util.message.v2.packet.MessagePacket
import catmoe.fallencrystal.moefilter.util.message.v2.packet.type.MessagesType
import catmoe.fallencrystal.moefilter.util.message.v2.packet.type.MessagesType.ACTION_BAR
import catmoe.fallencrystal.moefilter.util.message.v2.packet.type.MessagesType.CHAT
import catmoe.fallencrystal.moefilter.util.message.v2.processor.actionbar.ActionbarPacketProcessor
import catmoe.fallencrystal.moefilter.util.message.v2.processor.cache.MessagePacketCache
import catmoe.fallencrystal.moefilter.util.message.v2.processor.chat.ChatPacketProcessor
import catmoe.fallencrystal.moefilter.util.plugin.util.Scheduler
import catmoe.fallencrystal.translation.utils.component.ComponentUtil
import com.github.benmanes.caffeine.cache.Caffeine
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.connection.ProxiedPlayer
import java.util.concurrent.TimeUnit
import java.util.logging.Level

@Suppress("unused")
object MessageUtil {
    // Type(Enum's Field) + Message, Packet
    private val packetCache = Caffeine.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).build<String, MessagePacket>()
    private val bungee = ProxyServer.getInstance()
    private val logger = bungee.logger
    private val scheduler = Scheduler.getDefault()

    private fun packetBuilder(message: String, type: MessagesType, protocol: List<Int>) : MessagePacket {
        return when (type) {
            ACTION_BAR -> { ActionbarPacketProcessor().process(message, protocol) }
            CHAT -> { ChatPacketProcessor().process(message, protocol) }
        }
    }

    fun sendMessage(message: String, type: MessagesType, connection: ConnectionUtil) {
        scheduler.runAsync {
            var packet = packetCache.getIfPresent("${type.prefix}$message") ?: packetBuilder(message, type, listOf(connection.version))
            if (!packet.supportChecker(connection.version)) packet = packetBuilder(message, type, listOf(connection.version))
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
        val packet =  MessagePacketCache(type.processor).readCachedAndWrite(message) ?: packetBuilder(message, type, listOf(connection.version))
        packetSender(packet, connection)
    }

    fun sendMessage(message: String, type: MessagesType, sender: CommandSender) {
        val version = if (sender is ProxiedPlayer) sender.pendingConnection.version else 0
        val packet = MessagePacketCache(type.processor).readCachedAndWrite(message) ?: packetBuilder(message, type, listOf(version))
        if (sender is ProxiedPlayer) {
            val connection = ConnectionUtil(sender.pendingConnection)
            packetSender(packet, connection)
        } else { logInfo(packet.getOriginal()) }
    }

    fun invalidateCache(type: MessagesType, message: String) { packetCache.invalidate("${type.prefix}$message") }

    fun argsBuilder(startIndex: Int, args: Array<out String>?): StringBuilder {
        val message = StringBuilder()
        if (args != null) { for (i in startIndex until args.size - 1) { message.append(args[i]).append(" ") } }
        message.append(args?.get(args.size - 1) ?: return message)
        return message
    }

    fun logInfo(message: String) { logger.log(Level.INFO, logColorize(message)) }

    fun logWarn(message: String) { logger.log(Level.WARNING, logColorize(message)) }

    fun logError(message: String) { logger.log(Level.SEVERE, logColorize(message)) }

    fun colorize(message: String): BaseComponent { return ComponentUtil.toBaseComponents(ComponentUtil.parse(message))
        ?: return TextComponent("") }

    fun colorize(message: String, hex: Boolean): BaseComponent {
        val c = ComponentUtil.parse(message)
        return when (hex) {
            true -> { ComponentUtil.toBaseComponents(ComponentUtil.parse(message, true)) ?: return TextComponent("") }
            false -> { BungeeComponentSerializer.legacy().serialize(c)[0] }
        }
    }

    private fun logColorize(message: String): String {
        return if (message.contains("<") && message.contains(">")) colorize(message).toLegacyText() else message
    }
}
