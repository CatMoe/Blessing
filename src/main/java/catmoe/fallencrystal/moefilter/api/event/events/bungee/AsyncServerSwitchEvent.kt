package catmoe.fallencrystal.moefilter.api.event.events.bungee

import catmoe.fallencrystal.moefilter.api.event.MoeAsyncEvent
import net.md_5.bungee.api.config.ServerInfo
import net.md_5.bungee.api.connection.ProxiedPlayer

@Suppress("unused")
class AsyncServerSwitchEvent(val player: ProxiedPlayer, val from: ServerInfo?) : MoeAsyncEvent