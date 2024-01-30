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

import net.miaomoe.blessing.fallback.config.FallbackSettings
import net.miaomoe.blessing.protocol.util.ComponentUtil.toComponent
import net.miaomoe.blessing.protocol.util.ComponentUtil.toLegacyText
import net.miaomoe.blessing.protocol.packet.common.PacketPluginMessage
import net.miaomoe.blessing.protocol.packet.configuration.PacketRegistryData
import net.miaomoe.blessing.protocol.packet.login.PacketLoginResponse
import net.miaomoe.blessing.protocol.packet.play.*
import net.miaomoe.blessing.protocol.packet.type.PacketToClient
import net.miaomoe.blessing.protocol.util.ByteMessage
import net.miaomoe.blessing.protocol.version.Version
import net.miaomoe.blessing.protocol.version.VersionRange
import java.util.function.BiFunction
import java.util.logging.Level

enum class PacketsToCache(
    val packet: BiFunction<FallbackSettings, Version, PacketToClient?>,
    val description: String? = null,
    val version: VersionRange = VersionRange(Version.V1_7_2, Version.max)
) {
    REGISTRY_DATA({ settings, version ->
        if (version.moreOrEqual(Version.V1_20_2))
            PacketRegistryData(settings.world)
        else null
    }, "Cached PacketRegistryData", VersionRange(Version.V1_20_2, Version.max)),
    JOIN_GAME({ settings, _ -> PacketJoinGame(dimension = settings.world.dimension) }, "Cached PacketJoinGame"),
    PLUGIN_MESSAGE({ settings, version ->
        PacketPluginMessage(
            if (version.moreOrEqual(Version.V1_13_2)) "minecraft:brand" else "MC|Brand",
            ByteMessage.create().use { byteBuf ->
                byteBuf.writeString(settings.brand.toComponent().toLegacyText())
                byteBuf.toByteArray()
            }
        )
    }, "Cached PluginMessage (brand)"),
    LOGIN_RESPONSE({ settings, _ -> PacketLoginResponse(settings.playerName) }, "Cached LoginResponse"),
    SPAWN_POSITION({ settings, _ -> PacketSpawnPosition(settings.spawnPosition) }, "Cached SpawnPosition"),
    JOIN_POSITION({ settings, _ -> PacketPositionLook(settings.joinPosition, settings.teleportId) }, "Cached Teleport for joining"),
    PLAYER_ABILITIES(({ settings, _ -> PacketAbilities(if (settings.isDisableFall) 0x02 else 0x00, viewModifier = 0.1f) }), "Cached Player Abilities"),
    GAME_EVENT(({ _, _ -> PacketGameEvent(13) }), "Cached Game Event", VersionRange.of(Version.V1_20_3));

    fun getCacheGroup(settings: FallbackSettings): PacketCacheGroup? {
        val lastPacket = packet.apply(settings, this.version.max) ?: return null
        val group = PacketCacheGroup(lastPacket, copySame = true)
        var bytes = 0
        for (version in this.version) {
            val packet = this.packet.apply(settings, version) ?: continue
            val encoded = ByteMessage.create().use {
                packet.encode(it, version)
                bytes += it.readableBytes()
                it.toByteArray()
            }
            val cache = PacketCache(packet::class, encoded, this.description)
            group.setAt(version, cache)
        }
        settings.debugLogger?.log(Level.INFO, "Finished cached packet for $lastPacket. (With $bytes bytes)")
        return group
    }
}