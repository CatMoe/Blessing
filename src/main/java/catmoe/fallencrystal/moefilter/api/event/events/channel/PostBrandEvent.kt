package catmoe.fallencrystal.moefilter.api.event.events.channel

import io.netty.channel.Channel
import net.md_5.bungee.api.connection.ProxiedPlayer

class PostBrandEvent(val channel: Channel, val player: ProxiedPlayer, val brand: String)