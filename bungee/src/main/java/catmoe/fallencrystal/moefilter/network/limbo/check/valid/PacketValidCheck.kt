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

package catmoe.fallencrystal.moefilter.network.limbo.check.valid

import catmoe.fallencrystal.moefilter.network.common.exception.InvalidHandshakeException
import catmoe.fallencrystal.moefilter.network.common.exception.InvalidPacketException
import catmoe.fallencrystal.moefilter.network.limbo.check.Checker
import catmoe.fallencrystal.moefilter.network.limbo.check.LimboCheckType
import catmoe.fallencrystal.moefilter.network.limbo.check.LimboChecker
import catmoe.fallencrystal.moefilter.network.limbo.handler.LimboHandler
import catmoe.fallencrystal.moefilter.network.limbo.listener.HandlePacket
import catmoe.fallencrystal.moefilter.network.limbo.listener.ILimboListener
import catmoe.fallencrystal.moefilter.network.limbo.packet.LimboPacket
import catmoe.fallencrystal.moefilter.network.limbo.packet.c2s.PacketHandshake
import catmoe.fallencrystal.moefilter.network.limbo.packet.c2s.PacketInitLogin
import catmoe.fallencrystal.moefilter.network.limbo.packet.c2s.PacketStatusRequest
import catmoe.fallencrystal.moefilter.network.limbo.packet.common.Disconnect
import catmoe.fallencrystal.moefilter.network.limbo.packet.common.PacketKeepAlive
import catmoe.fallencrystal.moefilter.network.limbo.packet.common.PacketStatusPing
import catmoe.fallencrystal.moefilter.network.limbo.packet.common.Unknown
import catmoe.fallencrystal.translation.utils.version.Version
import com.github.benmanes.caffeine.cache.Caffeine

@Checker(LimboCheckType.VALID)
@HandlePacket(
    Unknown::class,
    Disconnect::class,
    PacketHandshake::class,
    /* Ping */
    PacketStatusRequest::class,
    PacketStatusPing::class,
    /* Join */
    PacketInitLogin::class,
    PacketKeepAlive::class,
)
object PacketValidCheck : LimboChecker, ILimboListener {

    private val allowUnknown = Caffeine.newBuilder().build<LimboHandler, Boolean>()
    private val next = Caffeine.newBuilder().build<LimboHandler, NextPacket>()

    private val w = listOf(0x17)

    override fun received(packet: LimboPacket, handler: LimboHandler, cancelledRead: Boolean): Boolean {
        if (packet is Disconnect) {
            allowUnknown.invalidate(handler)
            next.invalidate(handler)
        }
        if (!handler.channel.isActive) return true
        if (packet is Unknown && allowUnknown.getIfPresent(handler) == null) {
            if (handler.version == Version.V1_7_6 && w.contains(packet.id)) return false
            handler.channel.close()
            throw InvalidPacketException("This state is not allowed unknown packet. (${"0x%02X".format(packet.id)})")
        }
        if (packet is PacketHandshake) {
            when (packet.nextState.stateId) {
                1 -> next.put(handler, NextPacket.STATUS_REQUEST)
                2 -> next.put(handler, NextPacket.INIT_LOGIN)
                else -> throw InvalidHandshakeException("Must be 1 (Ping) or 2 (Join)")
            }
        }
        /*
        Ping
         */
        if (packet is PacketStatusRequest) {
            verify(packet, NextPacket.STATUS_REQUEST)
            next.put(handler, NextPacket.STATUS_PING)
        }
        if (packet is PacketStatusPing) { verify(packet, NextPacket.STATUS_PING) }

        /*
        Join
         */
        if (packet is PacketInitLogin) {
            verify(packet, NextPacket.INIT_LOGIN)
            next.put(handler, NextPacket.KEEP_ALIVE)
        }
        if (packet is PacketKeepAlive) {
            verify(packet, NextPacket.KEEP_ALIVE)
            allowUnknown.put(handler, true)
        }
        return false
    }

    private fun verify(packet: LimboPacket, next: NextPacket) {
        if (packet::class != next.packet) throw InvalidPacketException("Next packet should be ${next.packet.simpleName} but actually is ${packet::class.simpleName}")
    }

    override fun send(packet: LimboPacket, handler: LimboHandler, cancelled: Boolean): Boolean {
        return false
    }
}