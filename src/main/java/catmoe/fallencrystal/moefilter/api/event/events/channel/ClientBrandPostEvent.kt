package catmoe.fallencrystal.moefilter.api.event.events.channel

import io.netty.channel.Channel
import net.md_5.bungee.api.connection.ProxiedPlayer

@Suppress("unused")
class ClientBrandPostEvent(val channel: Channel, val player: ProxiedPlayer, val brand: String)