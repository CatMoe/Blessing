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