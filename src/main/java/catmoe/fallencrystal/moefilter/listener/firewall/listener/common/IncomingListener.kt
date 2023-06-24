package catmoe.fallencrystal.moefilter.listener.firewall.listener.common

import catmoe.fallencrystal.moefilter.listener.main.MainListener
import net.md_5.bungee.api.event.ClientConnectEvent
import net.md_5.bungee.api.event.PlayerHandshakeEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import net.md_5.bungee.event.EventPriority

class IncomingListener : Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onIncomingConnect(event: ClientConnectEvent) { event.isCancelled = MainListener.initConnection(event.socketAddress) }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onHandshake(event: PlayerHandshakeEvent) { MainListener.onHandshake(event.handshake, event.connection) }
}