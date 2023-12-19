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

package catmoe.fallencrystal.moefilter.network.limbo.packet.cache

import catmoe.fallencrystal.moefilter.network.limbo.LimboLocation
import catmoe.fallencrystal.moefilter.network.limbo.handler.LimboLoader
import catmoe.fallencrystal.moefilter.network.limbo.packet.cache.EnumPacket.*
import catmoe.fallencrystal.moefilter.network.limbo.packet.common.PacketPluginMessage
import catmoe.fallencrystal.moefilter.network.limbo.packet.s2c.*
import catmoe.fallencrystal.moefilter.util.message.v2.MessageUtil
import catmoe.fallencrystal.translation.utils.config.LocalConfig
import com.github.benmanes.caffeine.cache.Caffeine
import net.md_5.bungee.api.ProxyServer
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.ThreadLocalRandom

object LimboPacketCache {

    val packetCache = Caffeine.newBuilder().build<EnumPacket, PacketSnapshot>()
    private val loc = LimboLocation(7.5, LimboLoader.spawnHeight, 7.5, 90f, 10f, false)
    private val proxy = ProxyServer.getInstance()
    private val brand = MessageUtil.colorize(LocalConfig.getConfig().getString("f3-brand.custom")
        .replace("%bungee%", proxy.name)
        .replace("%version%", proxy.version)
        .replace("%backend%", "MoeLimbo")).toLegacyText()

    fun initPacket() {
        val username = "MoeLimbo"
        val uuid = UUID.nameUUIDFromBytes("OfflinePlayer:$username".toByteArray(StandardCharsets.UTF_8))
        val join = PacketJoinGame(reducedDebugInfo=LimboLoader.reduceDebug)

        packetCache.put(LOGIN_SUCCESS, PacketSnapshot.of(PacketLoginSuccess(uuid, username)))
        packetCache.put(JOIN_GAME, PacketSnapshot.of(join))
        packetCache.put(POS_AND_LOOK, PacketSnapshot.of(PacketServerPositionLook(loc, ThreadLocalRandom.current().nextInt())))
        packetCache.put(SPAWN_POSITION, PacketSnapshot.of(PacketSpawnPosition(loc)))
        packetCache.put(PLAYER_ABILITIES, PacketSnapshot.of(PacketPlayerAbilities(PacketPlayerAbilities.Flags.FLYING)))
        packetCache.put(PLAYER_INFO, PacketSnapshot.of(PacketPlayerInfo(join.gameMode, username, uuid)))

        packetCache.put(UPDATE_TIME, PacketSnapshot.of(PacketUpdateTime()))

        val pm = PacketPluginMessage("minecraft:brand", this.brand)
        packetCache.put(PLUGIN_MESSAGE, PacketSnapshot.of(pm))
        pm.channel="MC|Brand"
        packetCache.put(PLUGIN_MESSAGE_LEGACY, PacketSnapshot.of(pm))


        (-1..1).forEach { x -> (-1..1).forEach { z ->
            packetCache.put(EnumPacket.valueOf("CHUNK_${x+1}_${z+1}"), PacketSnapshot.of(PacketEmptyChunk(x, z)))
        }}

    }

}