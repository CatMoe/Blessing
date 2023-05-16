package catmoe.fallencrystal.moefilter.util

import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.connection.ProxiedPlayer

object MessageUtil {

    private val logger = ProxyServer.getInstance().logger

    fun colorize(text: String): String { return ChatColor.translateAlternateColorCodes('&', text) }

    fun messageBuilder(startIndex: Int, args: Array<out String>?): StringBuilder {
        val message = StringBuilder()
        if (args != null) { for (i in startIndex until args.size) { message.append(args.get(i)).append(" ") } }
        return message
    }

    fun sendMessage(sender: CommandSender, message: String) { if (sender !is ProxiedPlayer) { logInfo(message); return } else { sendMessage(sender, message)} }

    fun sendMessage(player: ProxiedPlayer, type: ChatMessageType, message: String) { player.sendMessage(type, TextComponent(colorize(message))) }

    fun sendMessage(player: ProxiedPlayer, message: String) { sendMessage(player, ChatMessageType.CHAT, message) }

    fun sendActionbar(player: ProxiedPlayer, message: String) { sendMessage(player, ChatMessageType.ACTION_BAR, message) }

    fun logInfo(text: String) { logger.info(colorize((text))) }

    fun logWarn(text: String) { logger.warning(colorize(text)) }
}