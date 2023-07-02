package catmoe.fallencrystal.moefilter.listener.firewall.listener.waterfall

import catmoe.fallencrystal.moefilter.listener.main.MainListener
import net.md_5.bungee.api.event.PlayerHandshakeEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import net.md_5.bungee.event.EventPriority

class IncomingListener : Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onIncomingConnect(event: io.github.waterfallmc.waterfall.event.ConnectionInitEvent) { event.isCancelled = MainListener.initConnection(event.remoteSocketAddress) }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onHandshake(event: PlayerHandshakeEvent) { MainListener.onHandshake(event.handshake, event.connection) }
}