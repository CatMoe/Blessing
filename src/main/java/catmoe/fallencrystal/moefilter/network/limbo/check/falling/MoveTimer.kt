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

package catmoe.fallencrystal.moefilter.network.limbo.check.falling

import catmoe.fallencrystal.moefilter.common.config.LocalConfig
import catmoe.fallencrystal.moefilter.network.common.kick.DisconnectType
import catmoe.fallencrystal.moefilter.network.common.kick.FastDisconnect
import catmoe.fallencrystal.moefilter.network.common.kick.ServerKickType
import catmoe.fallencrystal.moefilter.network.limbo.check.Checker
import catmoe.fallencrystal.moefilter.network.limbo.check.LimboCheckType
import catmoe.fallencrystal.moefilter.network.limbo.check.LimboChecker
import catmoe.fallencrystal.moefilter.network.limbo.handler.LimboHandler
import catmoe.fallencrystal.moefilter.network.limbo.listener.HandlePacket
import catmoe.fallencrystal.moefilter.network.limbo.listener.ILimboListener
import catmoe.fallencrystal.moefilter.network.limbo.packet.LimboPacket
import catmoe.fallencrystal.moefilter.network.limbo.packet.c2s.PacketClientLook
import catmoe.fallencrystal.moefilter.network.limbo.packet.c2s.PacketClientPosition
import catmoe.fallencrystal.moefilter.network.limbo.packet.c2s.PacketClientPositionLook
import catmoe.fallencrystal.moefilter.network.limbo.packet.common.Disconnect
import catmoe.fallencrystal.moefilter.network.limbo.packet.common.PacketKeepAlive
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.RemovalCause
import java.util.concurrent.TimeUnit

@Checker(LimboCheckType.FALLING_TIMER)
@HandlePacket(
    PacketKeepAlive::class,
    Disconnect::class,
    PacketClientLook::class,
    PacketClientPosition::class,
    PacketClientPositionLook::class,
)
object MoveTimer : LimboChecker, ILimboListener {

    private var conf = LocalConfig.getLimbo().getConfig("check.timer")
    private var decay = conf.getLong("decay")
    private var kickVL = conf.getInt("kick-vl")
    private var allowDelay = conf.getLong("allowed-delay")
    private val lastTime = Caffeine.newBuilder()
        .build<LimboHandler, Long>()
    val firstTime = Caffeine.newBuilder()
        .build<LimboHandler, Long>()
    private var vlCache = Caffeine.newBuilder()
        .expireAfterWrite(decay, TimeUnit.SECONDS)
        .removalListener { a: LimboHandler?, b: Int?, c: RemovalCause? -> decay(a, b, c) }
        .build<LimboHandler, Int>()

    private fun decay(handler: LimboHandler?, vl: Int?, cause: RemovalCause?) {
        if (cause != RemovalCause.EXPIRED || vl == 1 || handler?.channel?.isActive != true) return
        vlCache.put(handler, vl!!-1)
    }

    fun reload() {
        val conf = LocalConfig.getLimbo().getConfig("check.timer")
        val decay = MoveTimer.conf.getLong("decay")
        if (this.decay != decay) {
            vlCache = Caffeine.newBuilder()
                .expireAfterWrite(MoveTimer.decay, TimeUnit.SECONDS)
                .removalListener { a: LimboHandler?, b: Int?, c: RemovalCause? -> decay(a, b, c) }
                .build()
        }
        this.conf=conf
        kickVL = MoveTimer.conf.getInt("kick-vl")
        allowDelay = MoveTimer.conf.getLong("allowed-delay")
    }

    override fun received(packet: LimboPacket, handler: LimboHandler, cancelledRead: Boolean): Boolean {
        val now = System.currentTimeMillis()
        if (packet is PacketKeepAlive) {
            if (firstTime.getIfPresent(handler) == null) firstTime.put(handler, now)
        }
        if (packet is Disconnect) { vlCache.invalidate(handler); lastTime.invalidate(handler); return false }
        if (cancelledRead) return false
        val lastTime = this.lastTime.getIfPresent(handler)
        val c = (lastTime ?: now) - now
        if (c != 0.toLong() && c < allowDelay) {
            val vl = vlCache.getIfPresent(handler) ?: 0
            if (vl >= this.kickVL) {
                FastDisconnect.disconnect(handler.channel, DisconnectType.UNEXPECTED_PING, ServerKickType.MOELIMBO)
                return true
            }
            vlCache.put(handler, vl)
            vlCache.put(handler, vl)
        }
        this.lastTime.put(handler, now)
        return false
    }

    override fun send(packet: LimboPacket, handler: LimboHandler, cancelled: Boolean): Boolean {
        return false
    }
}