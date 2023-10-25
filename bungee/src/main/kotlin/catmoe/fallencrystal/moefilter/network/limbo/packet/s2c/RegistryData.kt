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