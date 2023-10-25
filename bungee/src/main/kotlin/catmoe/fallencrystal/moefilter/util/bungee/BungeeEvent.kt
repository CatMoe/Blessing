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

package catmoe.fallencrystal.moefilter.util.bungee

import catmoe.fallencrystal.moefilter.MoeFilterBungee
import catmoe.fallencrystal.moefilter.network.bungee.util.PipelineUtil
import catmoe.fallencrystal.moefilter.network.limbo.compat.FakeInitialHandler
import catmoe.fallencrystal.moefilter.network.limbo.util.BungeeSwitcher
import catmoe.fallencrystal.moefilter.util.bungee.ping_modifier.PingServerType
import catmoe.fallencrystal.moefilter.util.message.v2.MessageUtil
import catmoe.fallencrystal.moefilter.util.message.v2.packet.type.MessagesType
import catmoe.fallencrystal.moefilter.util.plugin.util.Scheduler
import catmoe.fallencrystal.translation.event.EventManager
import catmoe.fallencrystal.translation.event.events.player.*
import catmoe.fallencrystal.translation.player.PlayerInstance
import catmoe.fallencrystal.translation.player.TranslatePlayer
import catmoe.fallencrystal.translation.player.bungee.BungeePlayer
import catmoe.fallencrystal.translation.server.ServerInstance
import catmoe.fallencrystal.translation.server.TranslateServer
import catmoe.fallencrystal.translation.server.bungee.BungeeServer
import catmoe.fallencrystal.translation.utils.config.LocalConfig
import catmoe.fallencrystal.translation.utils.version.Version
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.config.ServerInfo
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.event.*
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import net.md_5.bungee.event.EventPriority
import java.net.InetSocketAddress

@Suppress("SpellCheckingInspection")
class BungeeEvent : Listener {

    private val proxy = ProxyServer.getInstance()

    @EventHandler(priority = EventPriority.LOWEST)
    fun onChat(event: ChatEvent) {
        val player = ProxyServer.getInstance().getPlayer(event.sender.toString())
        if (event.isProxyCommand && event.message.equals("/bungee", ignoreCase = true)) {
            Scheduler.getDefault().runAsync {
                MessageUtil.sendMessage(
                    listOf(
                        "<gradient:green:yellow>This server is running " +
                                "<aqua><hover:show_text:'<rainbow>${proxy.name} ${proxy.version}'>${proxy.name}</hover></aqua> & " +
                                "<gradient:#F9A8FF:#97FFFF>MoeFilter ${MoeFilterBungee.instance.description.version}</gradient> ❤</gradient>",
                        "<gradient:#9BCD9B:#FFE4E1><click:open_url:'https://github.com/CatMoe/MoeFilter/'>CatMoe/MoeFilter</click> @ " +
                                "<click:open_url:'https://www.miaomoe.net/'>miaomoe.net</click></gradient>"
                    ).joinToString("<reset><newline>")
                    , MessagesType.CHAT, player)
            }; event.isCancelled = true; return
        }
        val p = PlayerInstance.getCachedOrNull(player.uniqueId) ?: return
        val e = PlayerChatEvent(p, event.message, event.isProxyCommand)
        if (event.isCancelled) e.setCancelled()
        EventManager.callEvent(e)
        if (!event.isCancelled && e.isCancelled()) event.isCancelled=true
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onPostLogin(event: PostLoginEvent) {
        val player = TranslatePlayer(BungeePlayer(event.player))
        PlayerInstance.addToList(player)
        EventManager.callEvent(PlayerJoinEvent(player))
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onServerConnect(event: ServerConnectEvent) {
        val e = PlayerConnectServerEvent(false, getTranslateServer(event.target), getTranslatePlayer(event.player))
        e.isCancelled=event.isCancelled
        EventManager.callEvent(e)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onServerConnect(event: ServerConnectedEvent) {
        EventManager.callEvent(PlayerConnectServerEvent(true, getTranslateServer(event.server.info), getTranslatePlayer(event.player)))
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onServerSwitch(event: ServerSwitchEvent) {
        val from = event.from ?: return
        EventManager.callEvent(PlayerSwitchServerEvent(getTranslatePlayer(event.player), getTranslateServer(from), getTranslateServer(event.player.server.info)))
    }

    private fun getTranslateServer(s: ServerInfo): TranslateServer {
        return ServerInstance.getServer(s.name) ?: TranslateServer(BungeeServer(s))
    }

    private fun getTranslatePlayer(p: ProxiedPlayer): TranslatePlayer {
        return PlayerInstance.getPlayer(p.uniqueId) ?: TranslatePlayer(BungeePlayer(p))
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onDisconnect(event: PlayerDisconnectEvent) {
        PipelineUtil.invalidateChannel(event.player)
        val player = PlayerInstance.getCachedOrNull(event.player.name) ?: return
        EventManager.callEvent(PlayerLeaveEvent(player))
        PlayerInstance.removeFromList(player)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onPing(event: ProxyPingEvent) {
        val conf = LocalConfig.getConfig().getConfig("ping")
        val modInfo = event.response.modinfo
        val rawType = conf.getAnyRef("type")
        modInfo.type =
            (try { PingServerType.valueOf(rawType.toString()) }
            catch (_: IllegalArgumentException) {
                MessageUtil.logWarn("[MoeFilter] [Ping] Unknown fml server type: $rawType, Fallback to VANILLA")
                PingServerType.VANILLA
            }).name
        if (modInfo.type != PingServerType.FML.name) modInfo.modList = mutableListOf()
        val brand = conf.getString("brand")
        if (brand.isNotEmpty()) event.response.version.name=MessageUtil.colorize(brand).toLegacyText()
        var protocol = event.response.version.protocol
        val version = Version.of(protocol)
        if (!version.isSupported) protocol=0
        if (conf.getBoolean("protocol-always-unsupported")) protocol=0
        event.response.version.protocol=protocol
        if (event.connection.isLegacy
            && event.connection.version >= Version.V1_7_6.number
            && !BungeeSwitcher.connectToBungee((event.connection.socketAddress as InetSocketAddress).address)
            && event.connection is FakeInitialHandler) {
            try {
                val fake = event.connection as FakeInitialHandler
                fake.fakeLegacy=false
            } catch (_: NoSuchFieldException) { }
        }
    }
}