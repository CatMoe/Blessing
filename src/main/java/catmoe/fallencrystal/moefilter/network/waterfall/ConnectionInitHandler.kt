package catmoe.fallencrystal.moefilter.network.waterfall

import io.github.waterfallmc.waterfall.event.ConnectionInitEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler

class ConnectionInitHandler : Listener {
    @EventHandler
    fun onInit(event: ConnectionInitEvent) {
        val address = event.remoteSocketAddress
    }
}