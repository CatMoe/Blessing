/*
 * Copyright (C) 2023-2024. CatMoe / Blessing Contributors
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

package net.miaomoe.blessing.fallback.handler

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelOutboundHandlerAdapter
import io.netty.channel.ChannelPromise
import io.netty.util.concurrent.ScheduledFuture
import net.miaomoe.blessing.protocol.packet.common.PacketKeepAlive
import net.miaomoe.blessing.protocol.registry.State
import java.util.concurrent.TimeUnit

class KeepAliveScheduler(
    private val fallback: FallbackHandler,
    private val delay: Long
) : ChannelOutboundHandlerAdapter() {

    private var task: ScheduledFuture<*>? = null

    override fun write(ctx: ChannelHandlerContext?, msg: Any?, promise: ChannelPromise?) {
        if (msg is PacketKeepAlive && task == null) start()
        super.write(ctx, msg, promise)
    }

    private fun start() {
        task = fallback.channel.eventLoop().scheduleAtFixedRate({
            if (fallback.markDisconnect || fallback.state.let { it != State.CONFIGURATION && it != State.PLAY } || task == null) {
                cancel()
                return@scheduleAtFixedRate
            }
            fallback.write(PacketKeepAlive(), true)
        }, delay, delay, TimeUnit.MILLISECONDS)
    }

    private fun cancel() {
        task?.cancel(true)
    }

}