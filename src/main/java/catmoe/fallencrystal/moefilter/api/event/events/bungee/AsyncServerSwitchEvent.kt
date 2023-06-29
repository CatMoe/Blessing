package catmoe.fallencrystal.moefilter.api.event.events.bungee

import net.md_5.bungee.api.config.ServerInfo
import net.md_5.bungee.api.connection.ProxiedPlayer

@Suppress("unused")
class AsyncServerSwitchEvent(val player: ProxiedPlayer, val from: ServerInfo?)