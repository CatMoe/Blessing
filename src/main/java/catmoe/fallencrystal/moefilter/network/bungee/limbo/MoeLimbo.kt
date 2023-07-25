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

package catmoe.fallencrystal.moefilter.network.bungee.limbo

import catmoe.fallencrystal.moefilter.network.bungee.limbo.dimension.DimensionRegistry
import catmoe.fallencrystal.moefilter.network.bungee.limbo.dimension.DimensionType
import catmoe.fallencrystal.moefilter.network.bungee.limbo.packet.cache.PacketCache
import catmoe.fallencrystal.moefilter.network.bungee.limbo.util.handshake.HandshakeState

object MoeLimbo {

    val connections: MutableCollection<LimboHandler> = ArrayList()

    fun initDimension() {
        HandshakeState.values().forEach { HandshakeState.STATE_BY_ID[it.stateId] = it }
        val dimension = DimensionType.OVERWORLD
        DimensionRegistry
        DimensionRegistry.defaultDimension1_16 = DimensionRegistry.getDimension(dimension, DimensionRegistry.codec_1_16)
        DimensionRegistry.defaultDimension1_18_2 = DimensionRegistry.getDimension(dimension, DimensionRegistry.codec_1_18_2)
        PacketCache.initPacket()
    }

}