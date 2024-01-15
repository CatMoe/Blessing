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

import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import java.net.InetSocketAddress

@Suppress("MemberVisibilityCanBePrivate", "CanBeParameter")
class FallbackHandler(val channel: Channel) : ChannelInboundHandlerAdapter() {

    var address: InetSocketAddress = channel.remoteAddress() as InetSocketAddress

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any?) {
        // TODO
        super.channelRead(ctx, msg)
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        exceptionHandler?.exceptionCaught(ctx, cause) ?: super.exceptionCaught(ctx, cause)
    }

    companion object {
        var exceptionHandler: ExceptionHandler? = null
    }

}