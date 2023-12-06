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

package catmoe.fallencrystal.moefilter.network.bungee.initializer

import catmoe.fallencrystal.moefilter.network.common.ExceptionCatcher
import io.netty.channel.ChannelHandlerContext

class BungeeInitializer : AbstractInitializer(), IPipeline {
    override fun handlerAdded(ctx: ChannelHandlerContext) { try { super.handlerAdded(ctx) } finally { if (!ctx.isRemoved) { ctx.pipeline().remove(this) } } }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun exceptionCaught(ctx: ChannelHandlerContext?, cause: Throwable?) { ExceptionCatcher.handle(ctx!!.channel(), cause!!) }
}
