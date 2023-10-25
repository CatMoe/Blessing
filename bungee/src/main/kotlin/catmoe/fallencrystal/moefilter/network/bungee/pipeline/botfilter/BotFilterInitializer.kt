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

package catmoe.fallencrystal.moefilter.network.bungee.pipeline.botfilter

import catmoe.fallencrystal.moefilter.network.bungee.pipeline.AbstractInitializer
import catmoe.fallencrystal.moefilter.network.bungee.pipeline.IPipeline
import catmoe.fallencrystal.moefilter.network.bungee.util.event.EventCaller
import catmoe.fallencrystal.moefilter.network.common.ExceptionCatcher
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPipeline
import lombok.RequiredArgsConstructor
import net.md_5.bungee.api.config.ListenerInfo
import net.md_5.bungee.netty.PipelineUtils
import net.md_5.bungee.protocol.Varint21FrameDecoder

@RequiredArgsConstructor
class BotFilterInitializer : AbstractInitializer(), IPipeline {

    override fun handlerAdded(ctx: ChannelHandlerContext) {
        try {
            // Use original VarInt21FrameDecoder for BotFilter.
            super.handlerAdded(ctx)
            if (ctx.channel().isActive) { ctx.pipeline().replace(PipelineUtils.FRAME_DECODER, PipelineUtils.FRAME_DECODER, Varint21FrameDecoder()) }
        } finally { if (!ctx.isRemoved) { ctx.pipeline().remove(this) } }
    }

    override fun connectToBungee(
        ctx: ChannelHandlerContext,
        pipeline: ChannelPipeline,
        channel: Channel,
        eventCaller: EventCaller,
        listener: ListenerInfo
    ) {
        super.connectToBungee(ctx, pipeline, channel, eventCaller, listener)
        pipeline.replace(PipelineUtils.FRAME_DECODER, PipelineUtils.FRAME_DECODER, Varint21FrameDecoder())
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun exceptionCaught(ctx: ChannelHandlerContext?, cause: Throwable?) { ExceptionCatcher.handle(ctx!!.channel(), cause!!) }
}