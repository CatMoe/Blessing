package catmoe.fallencrystal.moefilter.network.bungee.util.event

import catmoe.fallencrystal.moefilter.api.logger.BCLogType
import catmoe.fallencrystal.moefilter.api.logger.LoggerManager
import catmoe.fallencrystal.moefilter.network.bungee.util.ExceptionCatcher
import io.netty.channel.Channel
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.config.ListenerInfo
import net.md_5.bungee.api.event.ClientConnectEvent

class EventCaller(val channel: Channel, val listener: ListenerInfo) {
    private val isWaterfall = LoggerManager.getType() == BCLogType.WATERFALL
    private val bungee = ProxyServer.getInstance()

    var type = EventCallMode.DISABLED
    fun call(type: EventCallMode) { if (type == this.type) { callEvent() } }

    private fun callEvent() {
        if (bungee.pluginManager.callEvent(ClientConnectEvent(channel.remoteAddress(), listener)).isCancelled) { channel.close() }
        if (isWaterfall) {
            io.github.waterfallmc.waterfall.event.ConnectionInitEvent(channel.remoteAddress(), listener) {
                result: io.github.waterfallmc.waterfall.event.ConnectionInitEvent, throwable: Throwable? ->
                if (result.isCancelled) { channel.close(); return@ConnectionInitEvent }
                if (throwable != null) { ExceptionCatcher.handle(channel, throwable) }
            }
        }
    }

    fun whenReload() { type=EventCallMode.READY_DECODING }
}