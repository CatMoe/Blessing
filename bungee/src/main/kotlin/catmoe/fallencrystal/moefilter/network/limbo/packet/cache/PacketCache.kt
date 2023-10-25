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

import catmoe.fallencrystal.moefilter.network.limbo.handler.MoeLimbo
import catmoe.fallencrystal.moefilter.network.limbo.packet.cache.EnumPacket.*
import catmoe.fallencrystal.moefilter.network.limbo.packet.common.PacketPluginMessage
import catmoe.fallencrystal.moefilter.network.limbo.packet.s2c.*
import catmoe.fallencrystal.moefilter.network.limbo.LimboLocation
import catmoe.fallencrystal.moefilter.util.message.v2.MessageUtil
import catmoe.fallencrystal.translation.utils.config.LocalConfig
import com.github.benmanes.caffeine.cache.Caffeine
import net.md_5.bungee.api.ProxyServer
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.ThreadLocalRandom

object PacketCache {

    val packetCache = Caffeine.newBuilder().build<EnumPacket, PacketSnapshot>()
    private val loc = LimboLocation(7.5, 450.0, 7.5, 90f, 10f, false)
    private val proxy = ProxyServer.getInstance()
    private val brand = MessageUtil.colorize(LocalConfig.getConfig().getString("f3-brand.custom")
        .replace("%bungee%", proxy.name)
        .replace("%version%", proxy.version)
        .replace("%backend%", "MoeLimbo")).toLegacyText()

    fun initPacket() {
        val username = "MoeLimbo"
        val uuid = UUID.nameUUIDFromBytes("OfflinePlayer:$username".toByteArray(StandardCharsets.UTF_8))

        // PostLogin
        val loginSuccess = PacketLoginSuccess()
        loginSuccess.username=username
        loginSuccess.uuid=uuid

        val join = PacketJoinGame()
        join.reducedDebugInfo=MoeLimbo.reduceDebug

        val abilities = PacketPlayerAbilities()

        val teleportId = ThreadLocalRandom.current().nextInt()

        val pal = PacketServerPositionLook()
        pal.sendLoc = loc
        pal.teleport=teleportId

        val spawnLocation = PacketSpawnPosition()
        spawnLocation.location = loc

        val info = PacketPlayerInfo()
        info.username=username
        info.gameMode=join.gameMode
        info.uuid=uuid

        packetCache.put(LOGIN_SUCCESS, PacketSnapshot.of(loginSuccess))
        packetCache.put(JOIN_GAME, PacketSnapshot.of(join))
        packetCache.put(POS_AND_LOOK, PacketSnapshot.of(pal))
        packetCache.put(SPAWN_POSITION, PacketSnapshot.of(spawnLocation))
        packetCache.put(PLAYER_ABILITIES, PacketSnapshot.of(abilities))
        packetCache.put(PLAYER_INFO, PacketSnapshot.of(info))

        packetCache.put(UPDATE_TIME, PacketSnapshot.of(PacketUpdateTime()))

        val pm = PacketPluginMessage()
        pm.message=this.brand
        pm.channel="minecraft:brand"
        packetCache.put(PLUGIN_MESSAGE, PacketSnapshot.of(pm))
        pm.channel="MC|Brand"
        packetCache.put(PLUGIN_MESSAGE_LEGACY, PacketSnapshot.of(pm))

        packetCache.put(REGISTRY_DATA, PacketSnapshot.of(RegistryData()))


        (-1..1).forEach { x -> (-1..1).forEach { z ->
            val chunk = PacketEmptyChunk()
            chunk.x=x; chunk.z=z
            val enum = EnumPacket.valueOf("CHUNK_${x+1}_${z+1}")
            packetCache.put(enum, PacketSnapshot.of(chunk))
        }}

    }

}