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
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.miaomoe.blessing.fallback.cache.PacketCache
import net.miaomoe.blessing.fallback.cache.PacketCacheGroup
import net.miaomoe.blessing.fallback.cache.PacketsToCache
import net.miaomoe.blessing.fallback.config.FallbackConfig
import net.miaomoe.blessing.fallback.handler.exception.ExceptionHandler
import net.miaomoe.blessing.protocol.handlers.TimeoutHandler
import net.miaomoe.blessing.protocol.handlers.VarintFrameDecoder
import net.miaomoe.blessing.protocol.handlers.VarintLengthEncoder
import net.miaomoe.blessing.protocol.packet.common.PacketPluginMessage
import net.miaomoe.blessing.protocol.packet.configuration.PacketRegistryData
import net.miaomoe.blessing.protocol.packet.login.PacketLoginResponse
import net.miaomoe.blessing.protocol.packet.play.PacketJoinGame
import net.miaomoe.blessing.protocol.util.ByteMessage
import net.miaomoe.blessing.protocol.version.Version
import net.miaomoe.blessing.protocol.version.VersionRange
import java.util.concurrent.TimeUnit

@Suppress("MemberVisibilityCanBePrivate")
class FallbackInitializer(val config: FallbackConfig) : ChannelInitializer<Channel>() {

    var exceptionHandler: ExceptionHandler? = null

    val cache = HashMap<PacketsToCache, PacketCacheGroup>()

    fun refreshCache() {
        cache[PacketsToCache.REGISTRY_DATA] = PacketCacheGroup(
            PacketRegistryData(config.world),
            "Cached PacketRegistryData", true,
            VersionRange.of(Version.V1_20_3)
        )
        cache[PacketsToCache.JOIN_GAME] = PacketCacheGroup(
            PacketJoinGame(world = config.world),
            "Cached PacketJoinGame", true,
            VersionRange(Version.V1_7_6, Version.V1_20_3)
        )
        cache[PacketsToCache.PLUGIN_MESSAGE] = PacketCacheGroup(
            PacketPluginMessage(), "Cached PluginMessage (brand)", true
        ).let {
            val byteArray = ByteMessage.create().use { byteBuf ->
                byteBuf.writeString(LegacyComponentSerializer.legacySection().serialize(MiniMessage.miniMessage().deserialize(config.brand)))
                byteBuf.toByteArray()
            }
            val legacy = PacketPluginMessage("MC|Brand", byteArray)
            val modern = PacketPluginMessage("minecraft:brand", byteArray)
            it.setAt(VersionRange(Version.V1_8, Version.V1_12_2), PacketCache.create(legacy, Version.V1_8))
            it.setAt(VersionRange(Version.V1_13, Version.V1_20_3), PacketCache.create(modern, Version.V1_13))
            it
        }
        cache[PacketsToCache.LOGIN_RESPONSE] = PacketCacheGroup(
            PacketLoginResponse(config.playerName),
            "Cached LoginResponse",
            true,
            VersionRange(Version.V1_7_6, Version.V1_20_3)
        )
    }

    public override fun initChannel(channel: Channel) {
        val pipeline = channel.pipeline()
        val handler = FallbackHandler(this, channel)
        pipeline.addLast(FRAME_DECODER, VarintFrameDecoder())
        pipeline.addLast(LENGTH_ENCODER, VarintLengthEncoder())
        pipeline.addLast(DECODER, handler.decoder)
        pipeline.addLast(ENCODER, handler.encoder)
        pipeline.addLast(HANDLER, handler)
        pipeline.addFirst(TIMEOUT_HANDLER, TimeoutHandler(config.timeout, TimeUnit.MILLISECONDS))
        config.debugLogger?.let { _ ->
            handler.debug("Initialization complete. Handlers: ")
            pipeline.forEach { handler.debug(" ${it.key} - ${it.value}") }
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
    }
}