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
import net.miaomoe.blessing.fallback.cache.ChunksCache
import net.miaomoe.blessing.fallback.cache.PacketCacheGroup
import net.miaomoe.blessing.fallback.cache.PacketsToCache
import net.miaomoe.blessing.fallback.config.FallbackSettings
import net.miaomoe.blessing.protocol.handlers.TimeoutHandler
import net.miaomoe.blessing.protocol.handlers.VarintFrameDecoder
import net.miaomoe.blessing.protocol.handlers.VarintLengthEncoder
import net.miaomoe.blessing.protocol.util.PositionUtil
import java.util.concurrent.TimeUnit

@Suppress("MemberVisibilityCanBePrivate")
class FallbackInitializer @JvmOverloads constructor(
    val settings: FallbackSettings = FallbackSettings.create()
) : ChannelInitializer<Channel>() {

    val cache: MutableMap<PacketsToCache, PacketCacheGroup> = settings.cacheMap

    var chunksCache: ChunksCache? = null

    init {
        if (settings.isUseCache) this.refreshCache()
    }

    fun refreshCache() {
        cache.clear()
        for (enum in PacketsToCache.entries) {
            if (cache.containsKey(enum)) continue
            enum.getCacheGroup(settings)?.let { cache[enum] = it }
        }
        chunksCache?.caches?.clear()
        chunksCache = ChunksCache.surround(PositionUtil.toChunkOffset(settings.joinPosition.position), 1)
    }

    public override fun initChannel(channel: Channel) {
        val pipeline = channel.pipeline()
        val handler = FallbackHandler(this, channel)
        pipeline.addLast(FRAME_DECODER, VarintFrameDecoder())
        pipeline.addLast(LENGTH_ENCODER, VarintLengthEncoder())
        pipeline.addLast(DECODER, handler.decoder)
        pipeline.addLast(ENCODER, handler.encoder)
        pipeline.addLast(HANDLER, handler)
        if (settings.isAliveScheduler) pipeline.addLast(KEEP_ALIVE_SCHEDULER, KeepAliveScheduler(handler, settings.aliveDelay))
        pipeline.addFirst(TIMEOUT_HANDLER, TimeoutHandler(settings.timeout, TimeUnit.MILLISECONDS))
        settings.initListener?.accept(handler, channel)
        settings.debugLogger?.let { _ ->
            handler.debug { "Initialization complete. Handlers: " }
            pipeline.forEach { handler.debug { " ${it.key} - ${it.value}" } }
        }
    }

    companion object {
        const val HANDLER = "fallback-handler"
        const val LENGTH_ENCODER = "fallback-length-encoder"
        const val FRAME_DECODER = "fallback-frame-decoder"
        const val ENCODER = "fallback-encoder"
        const val DECODER = "fallback-decoder"
        const val HAPROXY_DECODER = "fallback-haproxy-decoder"
        const val TIMEOUT_HANDLER = "fallback-timeout"
        const val KEEP_ALIVE_SCHEDULER = "fallback-keep-alive"
    }
}