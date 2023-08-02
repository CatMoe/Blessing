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

package catmoe.fallencrystal.moefilter.network.limbo.check.impl

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
import catmoe.fallencrystal.moefilter.network.limbo.packet.c2s.PacketClientPosition
import catmoe.fallencrystal.moefilter.network.limbo.packet.c2s.PacketClientPositionLook
import catmoe.fallencrystal.moefilter.network.limbo.packet.common.Disconnect
import catmoe.fallencrystal.moefilter.network.limbo.util.LimboLocation
import catmoe.fallencrystal.moefilter.util.message.v2.MessageUtil
import com.github.benmanes.caffeine.cache.Caffeine
import kotlin.math.abs

@Checker(LimboCheckType.FALLING_CHECK)
@HandlePacket(
    PacketClientPosition::class,
    PacketClientPositionLook::class,
    Disconnect::class
)
object FallingCheck: LimboChecker, ILimboListener {

    private val lastLocation = Caffeine.newBuilder().build<LimboHandler, LimboLocation>()


    override fun received(packet: LimboPacket, handler: LimboHandler) {
        if (packet is Disconnect) {
            lastLocation.invalidate(handler)
            return
        }

        val lastLocation = this.lastLocation.getIfPresent(handler)

        var x: Double? = lastLocation?.x ?: handler.location?.x
        var y: Double? = lastLocation?.y ?: handler.location?.y
        var z: Double? = lastLocation?.z ?: handler.location?.z
        var yaw: Float? = lastLocation?.yaw ?: handler.location?.yaw
        var pitch: Float? = lastLocation?.pitch ?: handler.location?.pitch
        var onGround: Boolean? = lastLocation?.onGround ?: handler.location?.onGround

        if (packet is PacketClientPosition) {
            val loc = packet.lastLoc!!
            x=loc.x; y=loc.y; z=loc.z; onGround=loc.onGround
        }
        if (packet is PacketClientPositionLook) {
            val loc = packet.readLoc!!
            x=loc.x; y=loc.y; z=loc.z; yaw=loc.yaw; pitch=loc.pitch; onGround=loc.onGround
        }
        MessageUtil.logInfo("x: $x, y:$y, z:$z, yaw:$yaw, pitch:$pitch, onGround:$onGround")
        this.lastLocation.put(handler, LimboLocation(
            x ?: 0.0,
            y ?: 0.0,
            z ?: 0.0,
            yaw ?: 0f,
            pitch ?: 0f,
            onGround ?: false)
        )
        // Wrong rotations turn check
        if (pitch != null) { if (abs(pitch) > 90) { kick(handler); return } }
        // Y-axis cannot more than last y.
        if ((lastLocation?.y ?: y!!) < y!!) { kick(handler); return }
        // OnGround cannot be true.
        if (onGround == true) { kick(handler); return }
        if (lastLocation != null) {
            val cx = calculateRange(lastLocation.x, x!!)
            val cy = calculateRange(lastLocation.y, y)
            val cz = calculateRange(lastLocation.z, z!!)
            if (cx > 2.88 || cz > 2.88) kick(handler)
            if (cy > 3.9200038147009764) kick(handler)
            MessageUtil.logInfo("Y: $y, Speed: $cy")
        }
    }

    private fun calculateRange(d1: Double, d2: Double): Double {
        if (d1 == d2) return 0.0
        return if (d1 > d2) d1 - d2 else d2 - d1
    }

    private fun kick(handler: LimboHandler) {
        FastDisconnect.disconnect(handler.channel, DisconnectType.DETECTED, ServerKickType.MOELIMBO)
    }

    override fun send(packet: LimboPacket, handler: LimboHandler, cancelled: Boolean): Boolean {
        return false
    }
}