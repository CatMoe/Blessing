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

package catmoe.fallencrystal.moefilter.network.common.traffic

import catmoe.fallencrystal.translation.utils.config.LocalConfig
import catmoe.fallencrystal.translation.utils.config.Reloadable
import io.netty.channel.ChannelPipeline

object TrafficManager : Reloadable {

    private var config = LocalConfig.getConfig().getConfig("packet-limiter")
    var limbo = LimiterProfile.readConfig(config.getConfig("limbo"))
    var proxy = LimiterProfile.readConfig(config.getConfig("proxy"))

    override fun reload() {
        config = LocalConfig.getConfig().getConfig("packet-limiter")
        limbo = LimiterProfile.readConfig(config.getConfig("limbo"))
        proxy = LimiterProfile.readConfig(config.getConfig("proxy"))
    }

    fun addLimiter(pipeline: ChannelPipeline, profile: LimiterProfile?, silentException: Boolean) {
        pipeline.addFirst(TrafficLimiter.NAME, (profile ?: return).createLimiter(silentException))
    }
}