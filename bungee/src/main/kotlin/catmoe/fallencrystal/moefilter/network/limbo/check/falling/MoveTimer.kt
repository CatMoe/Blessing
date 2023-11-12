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

package catmoe.fallencrystal.moefilter.network.limbo.check.falling

import catmoe.fallencrystal.moefilter.network.common.kick.DisconnectType
import catmoe.fallencrystal.moefilter.network.common.kick.FastDisconnect
import catmoe.fallencrystal.moefilter.network.limbo.check.AntiBotChecker
import catmoe.fallencrystal.moefilter.network.limbo.check.LimboCheckType
import catmoe.fallencrystal.moefilter.network.limbo.check.LimboChecker
import catmoe.fallencrystal.moefilter.network.limbo.handler.LimboHandler
import catmoe.fallencrystal.moefilter.network.limbo.listener.HandlePacket
import catmoe.fallencrystal.moefilter.network.limbo.packet.LimboPacket
import catmoe.fallencrystal.moefilter.network.limbo.packet.c2s.PacketClientLook
import catmoe.fallencrystal.moefilter.network.limbo.packet.c2s.PacketClientPosition
import catmoe.fallencrystal.moefilter.network.limbo.packet.c2s.PacketClientPositionLook
import catmoe.fallencrystal.moefilter.network.limbo.packet.common.Disconnect
import catmoe.fallencrystal.moefilter.network.limbo.packet.common.PacketKeepAlive
import catmoe.fallencrystal.translation.utils.config.LocalConfig
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.RemovalCause
import java.util.concurrent.TimeUnit

@AntiBotChecker(LimboCheckType.FALLING_TIMER)
@HandlePacket(
    PacketKeepAlive::class,
    Disconnect::class,
    PacketClientLook::class,
    PacketClientPosition::class,
    PacketClientPositionLook::class,
)
object MoveTimer : LimboChecker {

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

    override fun reload() {
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
        if (packet is PacketKeepAlive && firstTime.getIfPresent(handler) == null) firstTime.put(handler, now)
        if (packet is Disconnect) { vlCache.invalidate(handler); lastTime.invalidate(handler); return false }
        if (cancelledRead) return false
        val lastTime = this.lastTime.getIfPresent(handler)
        val c = (lastTime ?: now) - now
        if (c != 0.toLong() && c < allowDelay) {
            val vl = vlCache.getIfPresent(handler) ?: 0
            if (vl >= this.kickVL) {
                FastDisconnect.disconnect(handler, DisconnectType.UNEXPECTED_PING)
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

    override fun register() {
        // Do not need that.
    }

    override fun unregister() {
        // Do not need that.
    }
}