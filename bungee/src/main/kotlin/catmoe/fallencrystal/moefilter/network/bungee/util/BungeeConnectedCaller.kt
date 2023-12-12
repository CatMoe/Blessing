/*
 * Copyright (C) 2023-2023. CatMoe / MoeFilter Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package catmoe.fallencrystal.moefilter.network.bungee.util

import catmoe.fallencrystal.moefilter.api.logger.BCLogType
import catmoe.fallencrystal.moefilter.api.logger.LoggerManager
import catmoe.fallencrystal.moefilter.network.common.ExceptionCatcher
import io.netty.channel.Channel
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.config.ListenerInfo
import net.md_5.bungee.api.event.ClientConnectEvent

class BungeeConnectedCaller(val channel: Channel, val listener: ListenerInfo) {
    private val isWaterfall = LoggerManager.getType() == BCLogType.WATERFALL
    private val bungee = ProxyServer.getInstance()

    fun callEvent() {
        if (isWaterfall) {
            // import then will throw `NoClassDefFoundError` when they don't run waterfall (or forks)
            io.github.waterfallmc.waterfall.event.ConnectionInitEvent(channel.remoteAddress(), listener) {
                    result: io.github.waterfallmc.waterfall.event.ConnectionInitEvent, throwable: Throwable? ->
                if (result.isCancelled) {
                    if (channel.isActive) channel.close()
                    return@ConnectionInitEvent
                }
                if (throwable != null) { ExceptionCatcher.handle(channel, throwable) }
            }
        }
        if (bungee.pluginManager.callEvent(ClientConnectEvent(channel.remoteAddress(), listener)).isCancelled && channel.isActive) channel.close()
    }
}