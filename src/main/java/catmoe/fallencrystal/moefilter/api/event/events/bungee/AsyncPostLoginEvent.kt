package catmoe.fallencrystal.moefilter.api.event.events.bungee

import catmoe.fallencrystal.moefilter.api.event.MoeEvent
import net.md_5.bungee.api.connection.ProxiedPlayer

@Suppress("unused")
class AsyncPostLoginEvent(val player: ProxiedPlayer) : MoeEvent