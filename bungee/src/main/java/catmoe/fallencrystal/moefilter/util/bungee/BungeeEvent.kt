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

package catmoe.fallencrystal.moefilter.util.bungee

import catmoe.fallencrystal.moefilter.MoeFilterBungee
import catmoe.fallencrystal.moefilter.api.event.events.bungee.AsyncChatEvent
import catmoe.fallencrystal.moefilter.api.event.events.bungee.AsyncPostLoginEvent
import catmoe.fallencrystal.moefilter.api.event.events.bungee.AsyncServerConnectEvent
import catmoe.fallencrystal.moefilter.api.event.events.bungee.AsyncServerSwitchEvent
import catmoe.fallencrystal.moefilter.network.bungee.util.PipelineUtil
import catmoe.fallencrystal.moefilter.network.limbo.compat.FakeInitialHandler
import catmoe.fallencrystal.moefilter.network.limbo.util.BungeeSwitcher
import catmoe.fallencrystal.moefilter.util.bungee.ping_modifier.PingServerType
import catmoe.fallencrystal.moefilter.util.message.v2.MessageUtil
import catmoe.fallencrystal.moefilter.util.message.v2.packet.type.MessagesType
import catmoe.fallencrystal.moefilter.util.plugin.util.Scheduler
import catmoe.fallencrystal.translation.event.EventManager
import catmoe.fallencrystal.translation.event.events.player.PlayerChatEvent
import catmoe.fallencrystal.translation.event.events.player.PlayerJoinEvent
import catmoe.fallencrystal.translation.event.events.player.PlayerLeaveEvent
import catmoe.fallencrystal.translation.player.PlayerInstance
import catmoe.fallencrystal.translation.player.TranslatePlayer
import catmoe.fallencrystal.translation.player.bungee.BungeePlayer
import catmoe.fallencrystal.translation.utils.config.LocalConfig
import catmoe.fallencrystal.translation.utils.version.Version
import net.md_5.bungee.api.ProxyServer
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
        if (event.isProxyCommand && event.message.equals("/bungee")) {
            Scheduler(MoeFilterBungee.instance).runAsync {
                MessageUtil.sendMessage(listOf(
                        "<gradient:green:yellow>This server is running " +
                                "<aqua><hover:show_text:'<rainbow>${proxy.name} ${proxy.version}'>${proxy.name}</hover></aqua> & " +
                                "<gradient:#F9A8FF:#97FFFF>MoeFilter ${MoeFilterBungee.instance.description.version}</gradient> ❤</gradient>",
                        "<gradient:#9BCD9B:#FFE4E1><click:open_url:'https://github.com/CatMoe/MoeFilter/'>CatMoe/MoeFilter</click> @ " +
                                "<click:open_url:'https://www.miaomoe.net/'>miaomoe.net</click></gradient>"
                    ).joinToString("<reset><newline>"), MessagesType.CHAT, player)
            }; event.isCancelled = true; return
        }
        catmoe.fallencrystal.moefilter.api.event.EventManager.triggerEvent(AsyncChatEvent(
            player,
            event.isProxyCommand,
            (event.isCommand && !event.isProxyCommand),
            event.isCancelled,
            event.message
        ))
        val p = PlayerInstance.getOrNull(player.uniqueId) ?: return
        val e = PlayerChatEvent(p, event.message, event.isProxyCommand)
        if (event.isCancelled) e.setCancelled()
        EventManager.callEvent(e)
        if (!event.isCancelled && e.isCancelled()) event.isCancelled=true
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onPostLogin(event: PostLoginEvent) {
        catmoe.fallencrystal.moefilter.api.event.EventManager.triggerEvent(AsyncPostLoginEvent(event.player)) // old event...
        val player = TranslatePlayer(BungeePlayer(event.player))
        PlayerInstance.cacheUUID.put(player.getUniqueId(), player)
        PlayerInstance.cacheName.put(player.getName(), player)
        EventManager.callEvent(PlayerJoinEvent(player))
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onServerConnect(event: ServerConnectEvent){
        catmoe.fallencrystal.moefilter.api.event.EventManager.triggerEvent(AsyncServerConnectEvent(event.player, event.target, false, event.isCancelled)) // old event...
    }

    /*
    isCancelled is not available on this event
    so isCancelled is always false.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    fun onServerConnected(event: ServerConnectedEvent) {
        catmoe.fallencrystal.moefilter.api.event.EventManager.triggerEvent(AsyncServerConnectEvent(event.player, event.server.info, event.server.isConnected, false)) // old event...
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onServerSwitch(event: ServerSwitchEvent) {
        catmoe.fallencrystal.moefilter.api.event.EventManager.triggerEvent(AsyncServerSwitchEvent(event.player, event.from)) // old event...
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onDisconnect(event: PlayerDisconnectEvent) {
        PipelineUtil.invalidateChannel(event.player)
        val player = PlayerInstance.getOrNull(event.player.name) ?: return
        EventManager.callEvent(PlayerLeaveEvent(player))
        PlayerInstance.cacheUUID.invalidate(player.getUniqueId())
        PlayerInstance.cacheName.invalidate(player.getName())
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
        if (conf.getBoolean("protocol-always-unsupported")) event.response.version.protocol=0
        if (event.connection.isLegacy
            && event.connection.version >= Version.V1_7_6.number
            && !BungeeSwitcher.connectToBungee((event.connection.socketAddress as InetSocketAddress).address)
            && event.connection is FakeInitialHandler) {
            try {
                val fake = event.connection as FakeInitialHandler
                fake.fakeLegacy=false
            } catch (_: NoSuchFieldException) {}
        }
    }
}