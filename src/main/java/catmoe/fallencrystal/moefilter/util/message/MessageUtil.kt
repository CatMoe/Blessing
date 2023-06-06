package catmoe.fallencrystal.moefilter.util.message

import net.md_5.bungee.api.*
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.connection.ProxiedPlayer

object MessageUtil {

    private val logger = ProxyServer.getInstance().logger

    fun colorize(text: String): String { return ChatColor.translateAlternateColorCodes('&', text) }

    fun colorizeTextComponent(text: String): BaseComponent { return TextComponent(colorize(text)) }

    fun colorize(text: List<String>): List<String> {
        val returnText = mutableListOf<String>()
        for (it in text) { returnText.add(colorize(it)) }
        return returnText
    }

    fun messageBuilder(startIndex: Int, args: Array<out String>?): StringBuilder {
        val message = StringBuilder()
        if (args != null) { for (i in startIndex until args.size) { message.append(args[i]).append(" ") } }
        return message
    }

    fun sendMessage(sender: CommandSender, message: String) { if (sender !is ProxiedPlayer) { logInfo(message); return } else { sendMessage(sender, message) } }

    fun sendMessage(player: ProxiedPlayer, type: ChatMessageType, message: String) { player.sendMessage(type, colorizeTextComponent(message)) }

    fun sendMessage(player: List<ProxiedPlayer>, type: ChatMessageType, message: String) { player.forEach { it.sendMessage(type, colorizeTextComponent(message)) } }

    fun sendMessage(player: ProxiedPlayer, message: String) { sendMessage(player, ChatMessageType.CHAT, message) }

    fun sendActionbar(player: ProxiedPlayer, message: String) { sendMessage(player, ChatMessageType.ACTION_BAR, message) }

    fun logInfo(text: String) { logger.info(colorize((text))) }

    fun logInfoRaw(text: String) { logger.info(text) }

    fun logWarn(text: String) { logger.warning(colorize(text)) }

    fun logWarnRaw(text: String) { logger.warning(text) }

    fun sendTitle(p: ProxiedPlayer, title: String, subtitle: String, stay: Int, fadeIn: Int, fadeOut: Int) { titleBuilder(title, subtitle, stay, fadeIn, fadeOut).send(p) }

    fun sendTitle(p: ProxiedPlayer, title: BaseComponent, subtitle: BaseComponent, stay: Int, fadeIn: Int, fadeOut: Int) { titleBuilder(title, subtitle, stay, fadeIn, fadeOut).send(p) }

    fun sendTitle(p: List<ProxiedPlayer>, title: String, subtitle: String, stay: Int, fadeIn: Int, fadeOut: Int) { p.forEach { titleBuilder(title, subtitle, stay, fadeIn, fadeOut).send(it) } }

    fun sendTitle(p: List<ProxiedPlayer>, title: BaseComponent, subtitle: BaseComponent, stay: Int, fadeIn: Int, fadeOut: Int) { p.forEach { titleBuilder(title, subtitle, stay, fadeIn, fadeOut).send(it) } }

    fun titleBuilder(title: String, subtitle: String, stay: Int, fadeIn: Int, fadeOut: Int): Title { return titleBuilder(TextComponent(title), TextComponent(subtitle), stay, fadeIn, fadeOut) }

    fun titleBuilder(title: BaseComponent, subtitle: BaseComponent, stay: Int, fadeIn: Int, fadeOut: Int): Title {
        val t = ProxyServer.getInstance().createTitle()
        t.title(title); t.subTitle(subtitle); t.stay(stay); t.fadeIn(fadeIn); t.fadeOut(fadeOut); return t
    }
}