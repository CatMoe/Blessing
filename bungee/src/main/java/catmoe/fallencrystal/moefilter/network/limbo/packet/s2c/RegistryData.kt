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

package catmoe.fallencrystal.moefilter.network.limbo.packet.s2c

import catmoe.fallencrystal.moefilter.network.limbo.dimension.DimensionInterface
import catmoe.fallencrystal.moefilter.network.limbo.dimension.adventure.DimensionRegistry
import catmoe.fallencrystal.moefilter.network.limbo.dimension.llbit.StaticDimension
import catmoe.fallencrystal.moefilter.network.limbo.handler.MoeLimbo
import catmoe.fallencrystal.moefilter.network.common.ByteMessage
import catmoe.fallencrystal.moefilter.network.limbo.packet.LimboS2CPacket
import catmoe.fallencrystal.translation.utils.version.Version
import com.github.benmanes.caffeine.cache.Caffeine
import io.netty.buffer.Unpooled

class RegistryData  : LimboS2CPacket() {
    override fun encode(packet: ByteMessage, version: Version?) {
        if (version != Version.V1_20_2) return
        when (MoeLimbo.dimLoaderMode) {
            DimensionInterface.ADVENTURE ->
                //packet.writeCompoundTag(DimensionRegistry.codec_1_20)
                packet.writeHeadlessCompoundTag(DimensionRegistry.codec_1_20)
            DimensionInterface.LLBIT ->
                packet.writeHeadlessCompoundTag(StaticDimension.cacheDimension.getIfPresent(version)!!)
                //packet.writeCompoundTag(StaticDimension.dim.dimension.getAttributes(version))
        }
    }

    companion object {
        private val cacheData = Caffeine.newBuilder().build<Version, ByteArray>()

        fun getCachedRegistry(version: Version): ByteArray {
            return cacheData.getIfPresent(version) ?: createRegistryData(version)
        }

        fun createRegistryData(version: Version): ByteArray {
            val byteBuf = ByteMessage(Unpooled.buffer())
            RegistryData().encode(byteBuf, version)
            val result = byteBuf.toByteArray()
            byteBuf.release()
            cacheData.put(version, result)
            return result
        }
    }
}