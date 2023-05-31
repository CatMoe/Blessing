package catmoe.fallencrystal.moefilter.listener.main

import catmoe.fallencrystal.moefilter.api.event.EventManager
import catmoe.fallencrystal.moefilter.api.event.events.bungee.AsyncChatEvent
import catmoe.fallencrystal.moefilter.api.event.events.bungee.AsyncPostLoginEvent
import catmoe.fallencrystal.moefilter.api.event.events.bungee.AsyncServerConnectEvent
import catmoe.fallencrystal.moefilter.api.event.events.bungee.AsyncServerSwitchEvent
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.event.*
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler

class BungeeEvent : Listener {
    @EventHandler(priority = 0)
    fun onChat(event: ChatEvent) { EventManager.triggerEvent(
        AsyncChatEvent(
            ProxyServer.getInstance().getPlayer(event.sender.toString()),
            event.isProxyCommand,
            (event.isCommand && !event.isProxyCommand),
            event.isCancelled
        )) }

    @EventHandler(priority = 127)
    fun onPostLogin(event: PostLoginEvent) { EventManager.triggerEvent(AsyncPostLoginEvent(event.player)) }

    @EventHandler
    fun onServerConnect(event: ServerConnectEvent){ EventManager.triggerEvent(AsyncServerConnectEvent(event.player, event.target, false, event.isCancelled)) }

    @EventHandler // isCancelled is not available on this event so isCancelled is always false.
    fun onServerConnected(event: ServerConnectedEvent) { EventManager.triggerEvent(AsyncServerConnectEvent(event.player, event.server.info, event.server.isConnected, false)) }

    @EventHandler(priority = 0)
    fun onServerSwitch(event: ServerSwitchEvent) { EventManager.triggerEvent(AsyncServerSwitchEvent(event.player, event.from)) }
}