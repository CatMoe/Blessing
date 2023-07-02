package catmoe.fallencrystal.moefilter.api.event.events.channel

import catmoe.fallencrystal.moefilter.api.event.MoeEvent
import io.netty.channel.ChannelHandlerContext
import net.md_5.bungee.api.config.ListenerInfo

@Suppress("unused")
class ChannelInitEvent(val ctx: ChannelHandlerContext, val listener: ListenerInfo) : MoeEvent