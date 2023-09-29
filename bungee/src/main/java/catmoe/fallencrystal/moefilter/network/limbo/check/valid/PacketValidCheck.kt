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
import catmoe.fallencrystal.moefilter.network.limbo.packet.LimboPacket
import catmoe.fallencrystal.moefilter.network.limbo.packet.c2s.*
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
    LoginAcknowledged::class
)
object PacketValidCheck : LimboChecker {

    private val allowUnknown = Caffeine.newBuilder().build<LimboHandler, Boolean>()
    private val next = Caffeine.newBuilder().build<LimboHandler, NextPacket>()

    private val w = listOf(0x17)

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
                    //if (handler.version == Version.V1_7_6 && w.contains(packet.id)) return false
                    when (handler.version) {
                        Version.V1_7_6 -> { if (w.contains(packet.id)) return false }
                        Version.V1_20_2 -> { if (w.contains(packet.id)) return false }
                        else -> {}
                    }
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
            is LoginAcknowledged -> {
                verify(packet, NextPacket.LOGIN_ACK)
                next.put(handler, NextPacket.CONFIGURATION)
                // Next?
            }
            is FinishConfiguration -> {
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