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

package catmoe.fallencrystal.moefilter.network.bungee.handler

import catmoe.fallencrystal.moefilter.common.counter.ConnectionStatistics
import catmoe.fallencrystal.moefilter.data.BlockType
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.timeout.IdleState
import io.netty.handler.timeout.IdleStateEvent
import io.netty.handler.timeout.IdleStateHandler
import java.util.concurrent.TimeUnit

class TimeoutHandler @JvmOverloads constructor(timeout: Long, timeUnit: TimeUnit? = TimeUnit.MILLISECONDS) : IdleStateHandler(timeout, 0L, 0L, timeUnit) {
    private var closed = false

    @Deprecated("")
    constructor() : this(12L, TimeUnit.SECONDS)

    @Throws(Exception::class)
    override fun channelIdle(ctx: ChannelHandlerContext, idleStateEvent: IdleStateEvent) { assert(idleStateEvent.state() == IdleState.READER_IDLE); readTimedOut(ctx) }

    @Throws(Exception::class)
    private fun readTimedOut(ctx: ChannelHandlerContext) {
        if (!closed) { if (ctx.channel().isActive) { ctx.close(); ConnectionStatistics.countBlocked(BlockType.TIMEOUT) }; closed = true }
    }
}