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

package catmoe.fallencrystal.moefilter.network.limbo.check.impl

import catmoe.fallencrystal.moefilter.network.common.kick.DisconnectType
import catmoe.fallencrystal.moefilter.network.common.kick.FastDisconnect
import catmoe.fallencrystal.moefilter.network.limbo.check.AntiBotChecker
import catmoe.fallencrystal.moefilter.network.limbo.check.LimboCheckType
import catmoe.fallencrystal.moefilter.network.limbo.check.LimboChecker
import catmoe.fallencrystal.moefilter.network.limbo.handler.LimboHandler
import catmoe.fallencrystal.moefilter.network.limbo.listener.ListenPacket
import catmoe.fallencrystal.moefilter.network.limbo.packet.LimboPacket
import catmoe.fallencrystal.moefilter.network.limbo.packet.c2s.PacketClientLook
import catmoe.fallencrystal.moefilter.network.limbo.packet.c2s.PacketClientPosition
import catmoe.fallencrystal.moefilter.network.limbo.packet.c2s.PacketClientPositionLook
import catmoe.fallencrystal.moefilter.network.limbo.packet.common.Disconnect
import catmoe.fallencrystal.moefilter.network.limbo.packet.common.PacketPluginMessage
import catmoe.fallencrystal.moefilter.network.limbo.packet.common.PacketTransaction
import com.github.benmanes.caffeine.cache.Caffeine
import kotlin.random.Random

// This check is useless because clients don't response Transaction packet. (Maybe)
@AntiBotChecker(LimboCheckType.TRANSACTION)
@ListenPacket(
    PacketTransaction::class,
    Disconnect::class,
    PacketClientPosition::class,
    PacketClientPositionLook::class,
    PacketClientLook::class,
    PacketPluginMessage::class
)
object TransactionCheck : LimboChecker {

    private val cache = Caffeine.newBuilder().build<LimboHandler, PacketTransaction>()
    private val verified = Caffeine.newBuilder().build<LimboHandler, Boolean>()

    override fun received(packet: LimboPacket, handler: LimboHandler, cancelledRead: Boolean): Boolean {
        when (packet) {
            is PacketPluginMessage -> if (packet.isBrand) handler.sendPacket(PacketTransaction(id = Random.nextInt(-32767, 32767)))
            is PacketTransaction -> {
                if (verified.getIfPresent(handler) == true) return false
                val sent = cache.getIfPresent(handler)
                @Suppress("DEPRECATION")
                if (sent == null || !sent.accepted || sent.id != packet.id) { kick(handler); return false }
                verified.put(handler, true)
            }
            is Disconnect -> {
                cache.invalidate(handler)
                verified.invalidate(handler)
            }
            //is PacketKeepAlive -> if (handler.state == Protocol.PLAY && this.verified.getIfPresent(handler) != true) kick(handler)
            //else -> if (this.verified.getIfPresent(handler) != true) kick(handler)
        }
        return false
    }

    override fun send(packet: LimboPacket, handler: LimboHandler, cancelled: Boolean): Boolean {
        /*
        when (packet) {
            is Transaction -> cache.put(handler, packet)
            is Disconnect -> {}
            else -> if (this.verified.getIfPresent(handler) != true) kick(handler)
        }
         */
        if (packet is PacketTransaction) cache.put(handler, packet)
        return false
    }

    private fun kick(handler: LimboHandler) = FastDisconnect.disconnect(handler, DisconnectType.DETECTED)

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

    override fun reload() {
        /*
        This module does not need that.
         */
    }
}