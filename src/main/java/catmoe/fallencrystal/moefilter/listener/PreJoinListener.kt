package catmoe.fallencrystal.moefilter.listener

import net.md_5.bungee.api.event.PreLoginEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import java.net.InetSocketAddress

class PreJoinListener : Listener {
    @EventHandler
    fun onPreJoin(event: PreLoginEvent) {
        val address = (event.connection.socketAddress as InetSocketAddress).address.hostAddress

    }
}