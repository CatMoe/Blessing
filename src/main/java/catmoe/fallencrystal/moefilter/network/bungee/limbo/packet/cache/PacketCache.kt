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

package catmoe.fallencrystal.moefilter.network.bungee.limbo.packet.cache

import catmoe.fallencrystal.moefilter.network.bungee.limbo.packet.PacketSnapshot
import catmoe.fallencrystal.moefilter.network.bungee.limbo.packet.s2c.*
import catmoe.fallencrystal.moefilter.network.bungee.limbo.util.LimboLocation
import com.github.benmanes.caffeine.cache.Caffeine
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.ThreadLocalRandom

@Suppress("MemberVisibilityCanBePrivate")
object PacketCache {

    val packetCache = Caffeine.newBuilder().build<EnumPacket, PacketSnapshot>()

    fun initPacket() {
        val username = "MoeLimbo"
        val uuid = UUID.nameUUIDFromBytes("OfflinePlayer:$username".toByteArray(StandardCharsets.UTF_8))

        // PostLogin
        val loginSuccess = PacketLoginSuccess()
        loginSuccess.username=username
        loginSuccess.uuid=uuid

        val join = PacketJoinGame()

        val abilities = PacketPlayerAbilities()

        val teleportId = ThreadLocalRandom.current().nextInt()

        val pall = PacketServerPositionLook()
        val pal = PacketServerPositionLook()
        pall.sendLoc = LimboLocation(0.0, 64.0, 0.0, 0f, 0f, false)
        pall.teleport=teleportId
        pal.sendLoc = LimboLocation(0.0, 400.0, 0.0, 0f, 0f, false)
        pal.teleport=teleportId

        val spawnLocation = PacketSpawnPosition()
        spawnLocation.location = LimboLocation(0.0, 400.0, 0.0, 0f, 0f, false)

        val info = PacketPlayerInfo()
        info.username=username
        info.gameMode=join.gameMode
        info.uuid=uuid

        packetCache.put(EnumPacket.LOGIN_SUCCESS, PacketSnapshot.of(loginSuccess))
        packetCache.put(EnumPacket.JOIN_GAME, PacketSnapshot.of(join))
        packetCache.put(EnumPacket.POS_AND_LOOK_LEGACY, PacketSnapshot.of(pall))
        packetCache.put(EnumPacket.POS_AND_LOOK, PacketSnapshot.of(pal))
        packetCache.put(EnumPacket.SPAWN_POSITION, PacketSnapshot.of(spawnLocation))
        packetCache.put(EnumPacket.PLAYER_ABILITIES, PacketSnapshot.of(abilities))
        packetCache.put(EnumPacket.PLAYER_INFO, PacketSnapshot.of(info))

        val brand = PacketPluginMessage()
        brand.channel="minecraft:brand"
        brand.message="MoeFilter <- MoeLimbo"
        packetCache.put(EnumPacket.PLUGIN_MESSAGE, PacketSnapshot.of(brand))
    }

}