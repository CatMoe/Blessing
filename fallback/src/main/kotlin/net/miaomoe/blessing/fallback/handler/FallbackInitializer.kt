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
import io.netty.handler.codec.haproxy.HAProxyMessageDecoder
import net.miaomoe.blessing.fallback.handler.exception.ExceptionHandler
import org.jetbrains.kotlin.utils.addToStdlib.ifTrue

@Suppress("MemberVisibilityCanBePrivate")
object FallbackInitializer : ChannelInitializer<Channel>() {

    var haproxy: Boolean = false

    var exceptionHandler: ExceptionHandler? = null

    const val ENCODER = "fallback-encoder"
    const val DECODER = "fallback-decoder"
    const val HAPROXY_DECODER = "fallback-haproxy-decoder"

    override fun initChannel(channel: Channel) {
        val pipeline = channel.pipeline()
        val handler = FallbackHandler(channel)
        haproxy.ifTrue { pipeline.addFirst(HAPROXY_DECODER, HAProxyMessageDecoder(true)) }
        pipeline.addFirst(DECODER, handler.decoder)
        pipeline.addFirst(ENCODER, handler.encoder)
        pipeline.addLast(handler)
    }
}