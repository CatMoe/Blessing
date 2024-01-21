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

package net.miaomoe.blessing.fallback.cache

import net.miaomoe.blessing.fallback.config.FallbackConfig
import net.miaomoe.blessing.protocol.packet.configuration.PacketRegistryData
import net.miaomoe.blessing.protocol.packet.play.PacketJoinGame
import net.miaomoe.blessing.protocol.util.ByteMessage
import net.miaomoe.blessing.protocol.version.Version
import net.miaomoe.blessing.protocol.version.VersionRange

enum class CachedPackets(val group: PacketCacheGroup) {
    JOIN_GAME(PacketCacheGroup(
        PacketJoinGame(world = FallbackConfig.INSTANCE.world),
        "Cached join game packet",
        true,
        VersionRange.Companion.of(Version.V1_7_2, Version.V1_20_3))
    ),
    REGISTRY_DATA(
        PacketCacheGroup(
            PacketRegistryData(),
            "Cached registry data",
            true,
        ).let {
            VersionRange.of(Version.V1_20_3).forEach { version ->
                val tag = FallbackConfig.INSTANCE.world.toTag(version.toNbtVersion())
                it.setAt(version, PacketCache(PacketRegistryData::class, ByteMessage.create().use { byteBuf ->
                    PacketRegistryData(tag).encode(byteBuf, version)
                    byteBuf.toByteArray()
                }))
            }
            it
        }
    )
}