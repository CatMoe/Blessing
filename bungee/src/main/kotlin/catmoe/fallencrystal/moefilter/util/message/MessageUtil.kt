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

package catmoe.fallencrystal.moefilter.util.message

import catmoe.fallencrystal.translation.utils.component.ComponentUtil
import com.github.benmanes.caffeine.cache.Caffeine
import net.md_5.bungee.BungeeTitle
import net.md_5.bungee.api.*
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.connection.ProxiedPlayer
import java.util.concurrent.TimeUnit
import java.util.logging.Level

@Deprecated("Please use MessageUtil v2")
@Suppress("unused", "MemberVisibilityCanBePrivate")
object MessageUtil {

    private val logger = ProxyServer.getInstance().logger

    private val mmCache = Caffeine.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).build<String, BaseComponent>()

    fun colorize(text: String): String { return ChatColor.translateAlternateColorCodes('&', text) }

    fun colorizeMiniMessage(text: String): BaseComponent {
        val result = (mmCache.getIfPresent(text) ?: ComponentUtil.toBaseComponents(ComponentUtil.parse(text))) ?: return TextComponent("")
        mmCache.put(text, result); return result
    }

    fun colorizeTextComponent(text: String): BaseComponent { return TextComponent(colorize(text)) }

    fun colorize(text: List<String>): List<String> {
        val returnText = mutableListOf<String>()
        for (it in text) { returnText.add(colorize(it)) }
        return returnText
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

    fun logError(text: String) { logger.log(Level.SEVERE, text) }

    fun sendTitle(p: ProxiedPlayer, title: String, subtitle: String, stay: Int, fadeIn: Int, fadeOut: Int) { titleBuilder(title, subtitle, stay, fadeIn, fadeOut).send(p) }

    fun sendTitle(p: ProxiedPlayer, title: BaseComponent, subtitle: BaseComponent, stay: Int, fadeIn: Int, fadeOut: Int) { titleBuilder(title, subtitle, stay, fadeIn, fadeOut).send(p) }

    fun sendTitle(p: List<ProxiedPlayer>, title: String, subtitle: String, stay: Int, fadeIn: Int, fadeOut: Int) { p.forEach { titleBuilder(title, subtitle, stay, fadeIn, fadeOut).send(it) } }

    fun sendTitle(p: List<ProxiedPlayer>, title: BaseComponent, subtitle: BaseComponent, stay: Int, fadeIn: Int, fadeOut: Int) { p.forEach { titleBuilder(title, subtitle, stay, fadeIn, fadeOut).send(it) } }

    fun titleBuilder(title: String, subtitle: String, stay: Int, fadeIn: Int, fadeOut: Int): Title { return titleBuilder(TextComponent(title), TextComponent(subtitle), stay, fadeIn, fadeOut) }

    fun titleBuilder(title: BaseComponent, subtitle: BaseComponent, stay: Int, fadeIn: Int, fadeOut: Int): Title {
        val t = BungeeTitle()
        t.title(title); t.subTitle(subtitle); t.stay(stay); t.fadeIn(fadeIn); t.fadeOut(fadeOut); return t
    }
}