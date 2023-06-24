package catmoe.fallencrystal.moefilter.network.bungee.util.event

import catmoe.fallencrystal.moefilter.api.event.EventManager
import catmoe.fallencrystal.moefilter.api.event.events.channel.ChannelInitEvent
import catmoe.fallencrystal.moefilter.api.logger.BCLogType
import catmoe.fallencrystal.moefilter.api.logger.LoggerManager
import catmoe.fallencrystal.moefilter.network.bungee.util.ExceptionCatcher
import io.netty.channel.ChannelHandlerContext
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.config.ListenerInfo
import net.md_5.bungee.api.event.ClientConnectEvent

class EventCaller(val ctx: ChannelHandlerContext, val listener: ListenerInfo) {
    private val isWaterfall = LoggerManager.getType() == BCLogType.WATERFALL
    private val bungee = ProxyServer.getInstance()
    private val channel = ctx.channel()

    var type = EventCallMode.DISABLED
    fun call(type: EventCallMode) { if (type == this.type) { callEvent() } }

    private fun callEvent() {
        EventManager.triggerEvent(ChannelInitEvent(ctx, listener))
        if (isWaterfall) {
            io.github.waterfallmc.waterfall.event.ConnectionInitEvent(channel.remoteAddress(), listener) {
                    result: io.github.waterfallmc.waterfall.event.ConnectionInitEvent, throwable: Throwable? ->
                if (result.isCancelled) { if (channel.isActive) channel.close(); return@ConnectionInitEvent }
                if (throwable != null) { ExceptionCatcher.handle(channel, throwable) }
            }
        }
        if (bungee.pluginManager.callEvent(ClientConnectEvent(channel.remoteAddress(), listener)).isCancelled) { if (channel.isActive) channel.close() }
    }

    fun whenReload() { type=EventCallMode.READY_DECODING }
}