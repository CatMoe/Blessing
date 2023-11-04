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

package catmoe.fallencrystal.moefilter.network.limbo.check.valid

import catmoe.fallencrystal.moefilter.network.common.exception.InvalidHandshakeException
import catmoe.fallencrystal.moefilter.network.common.exception.InvalidPacketException
import catmoe.fallencrystal.moefilter.network.limbo.check.Checker
import catmoe.fallencrystal.moefilter.network.limbo.check.LimboCheckType
import catmoe.fallencrystal.moefilter.network.limbo.check.LimboChecker
import catmoe.fallencrystal.moefilter.network.limbo.handler.LimboHandler
import catmoe.fallencrystal.moefilter.network.limbo.listener.HandlePacket
import catmoe.fallencrystal.moefilter.network.limbo.packet.LimboPacket
import catmoe.fallencrystal.moefilter.network.limbo.packet.c2s.PacketHandshake
import catmoe.fallencrystal.moefilter.network.limbo.packet.c2s.PacketInitLogin
import catmoe.fallencrystal.moefilter.network.limbo.packet.c2s.PacketLoginAcknowledged
import catmoe.fallencrystal.moefilter.network.limbo.packet.c2s.PacketStatusRequest
import catmoe.fallencrystal.moefilter.network.limbo.packet.common.*
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
    PacketLoginAcknowledged::class
)
object PacketOrderCheck : LimboChecker {

    private val allowUnknown = Caffeine.newBuilder().build<LimboHandler, Boolean>()
    private val next = Caffeine.newBuilder().build<LimboHandler, NextPacket>()

    override fun reload() {
        /*
        This module does not need that.
        */
    }

    override fun received(packet: LimboPacket, handler: LimboHandler, cancelledRead: Boolean): Boolean {
        when (packet) {
            is Disconnect -> {
                allowUnknown.invalidate(handler)
                next.invalidate(handler)
            }
            is Unknown -> {
                if (allowUnknown.getIfPresent(handler) == null) {
                    handler.channel.close()
                    throw InvalidPacketException("This state is not allowed unknown packet. (${"0x%02X".format(packet.id)})")
                }
            }

            // Handshake
            is PacketHandshake -> {
                when (packet.nextState.stateId) {
                    1 -> next.put(handler, NextPacket.STATUS_REQUEST)
                    2 -> next.put(handler, NextPacket.INIT_LOGIN)
                    else -> throw InvalidHandshakeException("Must be 1 (Ping) or 2 (Join)")
                }
            }

            // Ping
            is PacketStatusRequest -> {
                verify(packet, NextPacket.STATUS_REQUEST)
                next.put(handler, NextPacket.STATUS_PING)
            }
            is PacketStatusPing -> {
                verify(packet, NextPacket.STATUS_PING)
                next.invalidate(handler)
            }

            // Join
            is PacketInitLogin -> {
                verify(packet, NextPacket.INIT_LOGIN)
                next.put(handler, if (handler.version!!.moreOrEqual(Version.V1_20_2)) NextPacket.LOGIN_ACK else NextPacket.KEEP_ALIVE)
            }
            is PacketLoginAcknowledged -> {
                verify(packet, NextPacket.LOGIN_ACK)
                next.put(handler, NextPacket.CONFIGURATION)
            }
            is PacketFinishConfiguration -> {
                verify(packet, NextPacket.CONFIGURATION)
                allowUnknown.put(handler, true)
            }
            is PacketKeepAlive -> {
                verify(packet, NextPacket.KEEP_ALIVE)
                allowUnknown.put(handler, true)
            }
        }
        //if (!handler.channel.isActive) return true
        return false
    }

    private fun verify(packet: LimboPacket, next: NextPacket) {
        if (packet::class != next.packet) throw InvalidPacketException("Next packet should be ${next.packet.simpleName} but actually is ${packet::class.simpleName}")
    }

    override fun send(packet: LimboPacket, handler: LimboHandler, cancelled: Boolean): Boolean {
        return false
    }

    override fun register() {
        /*
        This module does not need that.
         */
    }

    override fun unregister() {
        /*
        This module does not need that.
         */
    }
}