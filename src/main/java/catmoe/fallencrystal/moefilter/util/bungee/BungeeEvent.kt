package catmoe.fallencrystal.moefilter.util.bungee

import catmoe.fallencrystal.moefilter.api.event.EventManager
import catmoe.fallencrystal.moefilter.api.event.events.bungee.AsyncChatEvent
import catmoe.fallencrystal.moefilter.api.event.events.bungee.AsyncPostLoginEvent
import catmoe.fallencrystal.moefilter.api.event.events.bungee.AsyncServerConnectEvent
import catmoe.fallencrystal.moefilter.api.event.events.bungee.AsyncServerSwitchEvent
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.event.*
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import net.md_5.bungee.event.EventPriority

class BungeeEvent : Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    fun onChat(event: ChatEvent) { EventManager.triggerEvent(
        AsyncChatEvent(
            ProxyServer.getInstance().getPlayer(event.sender.toString()),
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
}