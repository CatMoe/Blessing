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

package catmoe.fallencrystal.moefilter.network.limbo.packet.cache

import catmoe.fallencrystal.moefilter.network.limbo.packet.cache.EnumPacket.*
import catmoe.fallencrystal.moefilter.network.limbo.packet.s2c.*
import catmoe.fallencrystal.moefilter.network.limbo.util.LimboLocation
import com.github.benmanes.caffeine.cache.Caffeine
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.ThreadLocalRandom

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

        val pal = PacketServerPositionLook()
        pal.sendLoc = LimboLocation(8.0, 450.0, 8.0, 90f, 10f, false)
        pal.teleport=teleportId

        val spawnLocation = PacketSpawnPosition()
        spawnLocation.location = LimboLocation(8.0, 450.0, 8.0, 90f, 10f, false)

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

        (-1..1).forEach { x -> (-1..1).forEach { z ->
            val chunk = PacketEmptyChunk()
            chunk.x=x; chunk.z=z
            val enum = EnumPacket.valueOf("CHUNK_${x+1}_${z+1}")
            packetCache.put(enum, PacketSnapshot.of(chunk))
        }}

    }

}