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

package net.miaomoe.blessing.protocol.handlers

import io.netty.channel.ChannelHandlerContext
import io.netty.handler.timeout.IdleStateEvent
import io.netty.handler.timeout.IdleStateHandler
import org.jetbrains.kotlin.utils.addToStdlib.ifTrue
import java.util.concurrent.TimeUnit

class TimeoutHandler(timeout: Long, unit: TimeUnit) : IdleStateHandler(timeout, 0L, 0L, unit) {
    private var knownDisconnect = false
    override fun channelIdle(ctx: ChannelHandlerContext, evt: IdleStateEvent) {
        super.channelIdle(ctx, evt)
        if (!knownDisconnect) {
            ctx.channel().isActive.ifTrue { ctx.close() }
            knownDisconnect=true
        }
    }

}