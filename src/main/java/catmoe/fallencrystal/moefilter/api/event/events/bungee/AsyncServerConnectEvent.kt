package catmoe.fallencrystal.moefilter.api.event.events.bungee

import net.md_5.bungee.api.config.ServerInfo
import net.md_5.bungee.api.connection.ProxiedPlayer

@Suppress("unused")
class AsyncServerConnectEvent(val player: ProxiedPlayer, val server: ServerInfo, val isConnected: Boolean, val isCancelled: Boolean)