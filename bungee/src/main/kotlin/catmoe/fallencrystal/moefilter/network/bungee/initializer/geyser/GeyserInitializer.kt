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

package catmoe.fallencrystal.moefilter.network.bungee.initializer.geyser

import io.netty.channel.Channel
import io.netty.handler.codec.haproxy.HAProxyMessageDecoder
import net.md_5.bungee.BungeeCord
import net.md_5.bungee.connection.InitialHandler
import net.md_5.bungee.netty.HandlerBoss
import net.md_5.bungee.netty.PipelineUtils
import net.md_5.bungee.protocol.*

class GeyserInitializer {
    private val legacyKicker = KickStringWriter()

    @Throws(Exception::class)
    fun handle(channel: Channel, protocol: Int) {
        if (channel.remoteAddress() == null) { channel.close(); return }
        val listener = channel.attr(PipelineUtils.LISTENER).get()
        PipelineUtils.BASE.initChannel(channel)
        val pipeline = channel.pipeline()
        pipeline.addBefore(PipelineUtils.FRAME_DECODER, PipelineUtils.LEGACY_DECODER, LegacyDecoder())
        pipeline.addAfter(PipelineUtils.FRAME_DECODER, PipelineUtils.PACKET_DECODER, MinecraftDecoder(Protocol.HANDSHAKE, true, protocol))
        pipeline.addAfter(PipelineUtils.FRAME_PREPENDER, PipelineUtils.PACKET_ENCODER, MinecraftEncoder(Protocol.HANDSHAKE, true, protocol))
        pipeline.addBefore(PipelineUtils.FRAME_PREPENDER, PipelineUtils.LEGACY_KICKER, legacyKicker)
        channel.pipeline()[HandlerBoss::class.java].setHandler(InitialHandler(BungeeCord.getInstance(), listener))

        if (listener.isProxyProtocol) { channel.pipeline().addFirst(HAProxyMessageDecoder()) }
    }

    companion object {
        fun isGeyser(channel: Channel): Boolean { return channel.parent() != null && channel.parent().javaClass.canonicalName.startsWith("org.geysermc.geyser") }
    }
}
