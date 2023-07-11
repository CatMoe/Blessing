/*
 * Copyright 2023. CatMoe / FallenCrystal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
            // import then will throw `NoClassDefFoundError` when they don't run waterfall (or forks)
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