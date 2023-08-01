/*
 * Copyright 2023. CatMoe / FallenCrystal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package catmoe.fallencrystal.moefilter.network.limbo.netty

import catmoe.fallencrystal.moefilter.common.counter.ConnectionCounter
import catmoe.fallencrystal.moefilter.common.counter.type.BlockType
import catmoe.fallencrystal.moefilter.common.firewall.Firewall
import catmoe.fallencrystal.moefilter.common.firewall.Throttler
import catmoe.fallencrystal.moefilter.network.bungee.decoder.VarIntFrameDecoder
import catmoe.fallencrystal.moefilter.network.bungee.handler.TimeoutHandler
import catmoe.fallencrystal.moefilter.network.bungee.pipeline.AbstractPipeline
import catmoe.fallencrystal.moefilter.network.bungee.util.event.EventCallMode
import catmoe.fallencrystal.moefilter.network.bungee.util.event.EventCaller
import catmoe.fallencrystal.moefilter.network.limbo.handler.LimboHandler
import io.netty.channel.ChannelHandlerContext
import net.md_5.bungee.BungeeCord
import net.md_5.bungee.netty.PipelineUtils
import java.net.InetSocketAddress

class LimboPipeline : AbstractPipeline() {

    override fun handlerAdded(ctx: ChannelHandlerContext) {
        try {

            val channel = ctx.channel()
            val remoteAddress = if (channel.remoteAddress() == null) channel.parent().localAddress() else channel.remoteAddress()
            val inetAddress = (remoteAddress as InetSocketAddress).address
            val pipeline = channel.pipeline()
            val listener = channel.attr(PipelineUtils.LISTENER).get()
            val eventCaller = EventCaller(ctx, listener)

            ConnectionCounter.increase(inetAddress)
            eventCaller.call(EventCallMode.AFTER_INIT)
            if (Firewall.isFirewalled(inetAddress)) { channel.close(); ConnectionCounter.countBlocked(BlockType.FIREWALL); return }
            eventCaller.call(EventCallMode.NON_FIREWALL)
            if (Throttler.increase(inetAddress)) { channel.close(); ConnectionCounter.countBlocked(BlockType.FIREWALL); return }
            if (throttler != null && throttler.throttle(remoteAddress)) { channel.close(); ConnectionCounter.countBlocked(BlockType.FIREWALL); return }
            eventCaller.call(EventCallMode.READY_DECODING)

            if (!channel.isActive) { return }

            val decoder = LimboDecoder(null)
            val encoder = LimboEncoder(null)
            val handler = LimboHandler(encoder, decoder, channel, ctx)
            decoder.handler=handler
            encoder.handler=handler
            pipeline.addLast(PipelineUtils.TIMEOUT_HANDLER, TimeoutHandler(BungeeCord.getInstance().config.timeout.toLong()))
            pipeline.addLast(PipelineUtils.FRAME_DECODER, VarIntFrameDecoder())
            pipeline.addLast(PipelineUtils.FRAME_PREPENDER, VarIntLengthEncoder())
            pipeline.addLast(PipelineUtils.PACKET_DECODER, decoder)
            pipeline.addLast(PipelineUtils.PACKET_ENCODER, encoder)
            pipeline.addLast(PipelineUtils.BOSS_HANDLER, handler)
        } finally {
            if (!ctx.isRemoved) ctx.pipeline().remove(this)
        }
    }

}