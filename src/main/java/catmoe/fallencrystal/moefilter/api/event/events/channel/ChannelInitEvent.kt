package catmoe.fallencrystal.moefilter.api.event.events.channel

import io.netty.channel.ChannelHandlerContext
import net.md_5.bungee.api.config.ListenerInfo

class ChannelInitEvent(val ctx: ChannelHandlerContext, val listener: ListenerInfo)