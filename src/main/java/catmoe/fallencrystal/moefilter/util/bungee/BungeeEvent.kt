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

import catmoe.fallencrystal.moefilter.MoeFilter
import catmoe.fallencrystal.moefilter.api.event.EventManager
import catmoe.fallencrystal.moefilter.api.event.events.bungee.AsyncChatEvent
import catmoe.fallencrystal.moefilter.api.event.events.bungee.AsyncPostLoginEvent
import catmoe.fallencrystal.moefilter.api.event.events.bungee.AsyncServerConnectEvent
import catmoe.fallencrystal.moefilter.api.event.events.bungee.AsyncServerSwitchEvent
import catmoe.fallencrystal.moefilter.common.config.LocalConfig
import catmoe.fallencrystal.moefilter.network.bungee.util.PipelineUtil
import catmoe.fallencrystal.moefilter.util.bungee.ping_modifier.PingServerType
import catmoe.fallencrystal.moefilter.util.message.v2.MessageUtil
import catmoe.fallencrystal.moefilter.util.message.v2.packet.type.MessagesType
import catmoe.fallencrystal.moefilter.util.plugin.util.Scheduler
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.event.*
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import net.md_5.bungee.event.EventPriority

@Suppress("SpellCheckingInspection")
class BungeeEvent : Listener {

    private val proxy = ProxyServer.getInstance()

    @EventHandler(priority = EventPriority.LOWEST)
    fun onChat(event: ChatEvent) {
        val player = ProxyServer.getInstance().getPlayer(event.sender.toString())
        if (event.isProxyCommand && event.message.equals("/bungee")) {
            Scheduler(MoeFilter.instance).runAsync {
                MessageUtil.sendMessage(listOf(
                        "<gradient:green:yellow>This server is running " +
                                "<aqua><hover:show_text:'<rainbow>${proxy.name} ${proxy.version}'>${proxy.name}</hover></aqua> & " +
                                "<gradient:#F9A8FF:#97FFFF>MoeFilter ${MoeFilter.instance.description.version}</gradient> ❤</gradient>",
                        "<gradient:#9BCD9B:#FFE4E1><click:open_url:'https://github.com/CatMoe/MoeFilter/'>CatMoe/MoeFilter</click> @ " +
                                "<click:open_url:'https://www.miaomoe.net/'>miaomoe.net</click></gradient>"
                    ).joinToString("<reset><newline>"), MessagesType.CHAT, player)
            }; event.isCancelled = true; return
        }
        EventManager.triggerEvent(AsyncChatEvent(
            player,
            event.isProxyCommand,
            (event.isCommand && !event.isProxyCommand),
            event.isCancelled,
            event.message
        )) }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onPostLogin(event: PostLoginEvent) { EventManager.triggerEvent(AsyncPostLoginEvent(event.player)) }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onServerConnect(event: ServerConnectEvent){ EventManager.triggerEvent(AsyncServerConnectEvent(event.player, event.target, false, event.isCancelled)) }

    /*
    isCancelled is not available on this event
    so isCancelled is always false.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    fun onServerConnected(event: ServerConnectedEvent) { EventManager.triggerEvent(AsyncServerConnectEvent(event.player, event.server.info, event.server.isConnected, false)) }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onServerSwitch(event: ServerSwitchEvent) { EventManager.triggerEvent(AsyncServerSwitchEvent(event.player, event.from)) }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onDisconnect(event: PlayerDisconnectEvent) { PipelineUtil.invalidateChannel(event.player) }

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
        // If modList is null, The client will disconnect when reading data.
        if (modInfo.type != PingServerType.FML.name) modInfo.modList = mutableListOf()
        val brand = conf.getString("brand")
        if (brand.isNotEmpty()) event.response.version.name=MessageUtil.colorize(brand).toLegacyText()
        if (conf.getBoolean("protocol-always-unsupported")) event.response.version.protocol=0
    }
}