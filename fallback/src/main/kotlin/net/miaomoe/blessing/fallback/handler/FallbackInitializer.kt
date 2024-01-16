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
import io.netty.channel.ChannelInitializer

object FallbackInitializer : ChannelInitializer<Channel>() {

    var haproxy: Boolean = false

    override fun initChannel(channel: Channel) {
        val pipeline = channel.pipeline()
        val handler = FallbackHandler(channel)
        pipeline.addFirst(handler.decoder)
        pipeline.addFirst(handler.encoder)
        pipeline.addLast(handler)
    }
}