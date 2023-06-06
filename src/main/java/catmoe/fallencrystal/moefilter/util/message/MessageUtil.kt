package catmoe.fallencrystal.moefilter.util.message

import catmoe.fallencrystal.moefilter.util.message.component.ComponentUtil
import net.md_5.bungee.api.*
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.connection.ProxiedPlayer

object MessageUtil {

    private val logger = ProxyServer.getInstance().logger

    fun colorize(text: String): String { return ChatColor.translateAlternateColorCodes('&', text) }

    fun colorizeMiniMessage(text: String): BaseComponent { return ComponentUtil.toBaseComponents(ComponentUtil.parse(text)) }

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

    fun sendMessage(sender: CommandSender, bc: BaseComponent) { if (sender !is ProxiedPlayer) { logInfo(bc.toLegacyText()) } else { sendMessage(sender, bc) } }

    fun sendMessage(player: ProxiedPlayer, type: ChatMessageType, message: String) { player.sendMessage(type, colorizeTextComponent(message)) }

    fun sendMessage(player: List<ProxiedPlayer>, type: ChatMessageType, message: String) { player.forEach { it.sendMessage(type, colorizeTextComponent(message)) } }
    fun sendMessage(player: ProxiedPlayer, type: ChatMessageType, bc: BaseComponent) { player.sendMessage(type, bc) }

    fun sendMessage(player: List<ProxiedPlayer>, type: ChatMessageType, bc: BaseComponent) { player.forEach { sendMessage(it, type, bc) } }

    fun sendMessage(player: ProxiedPlayer, message: String) { sendMessage(player, ChatMessageType.CHAT, message) }
    fun sendMessage(player: ProxiedPlayer, bc: BaseComponent) { sendMessage(player, ChatMessageType.CHAT, bc) }

    fun sendActionbar(player: ProxiedPlayer, message: String) { sendMessage(player, ChatMessageType.ACTION_BAR, message) }
    fun sendActionbar(player: List<ProxiedPlayer>, message: String) { player.forEach { sendActionbar(it, message) } }
    fun sendActionbar(player: ProxiedPlayer, bc: BaseComponent) { player.sendMessage(ChatMessageType.ACTION_BAR, bc) }
    fun sendActionbar(player: List<ProxiedPlayer>,  bc: BaseComponent) { player.forEach { sendActionbar(it, bc) } }

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