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

import catmoe.fallencrystal.moefilter.common.counter.ConnectionStatistics
import catmoe.fallencrystal.moefilter.data.BlockType
import catmoe.fallencrystal.moefilter.common.firewall.Firewall
import catmoe.fallencrystal.moefilter.network.bungee.pipeline.MoeChannelHandler
import catmoe.fallencrystal.moefilter.network.limbo.check.AntiBotChecker
import catmoe.fallencrystal.moefilter.network.limbo.check.LimboCheckType
import catmoe.fallencrystal.moefilter.network.limbo.check.LimboChecker
import catmoe.fallencrystal.moefilter.network.limbo.handler.LimboHandler
import catmoe.fallencrystal.moefilter.network.limbo.listener.ListenPacket
import catmoe.fallencrystal.moefilter.network.limbo.packet.LimboPacket
import catmoe.fallencrystal.moefilter.network.limbo.packet.common.Disconnect
import java.net.InetSocketAddress

@Suppress("unused")
@AntiBotChecker(LimboCheckType.INSTANT_DISCONNECT)
@ListenPacket(Disconnect::class)
object InstantDisconnectCheck : LimboChecker {

    override fun reload() {
        // This module does not need to reload
    }

    override fun received(packet: LimboPacket, handler: LimboHandler, cancelledRead: Boolean): Boolean {
        val address = (handler.address as InetSocketAddress).address
        if (Firewall.isFirewalled(address)) return false
        /*
        if (MoeLimbo.sentHandshake.getIfPresent(handler) != true) {
            Firewall.addAddressTemp(address)
            ConnectionCounter.countBlocked(BlockType.FIREWALL)
        }
         */
        if (MoeChannelHandler.sentHandshake.getIfPresent(handler.channel) != true) {
            Firewall.addAddressTemp(address)
            ConnectionStatistics.countBlocked(BlockType.FIREWALL)
        }
        return false
    }

    override fun send(packet: LimboPacket, handler: LimboHandler, cancelled: Boolean): Boolean { return false }

    override fun register() {
        // This module does not need to do something on registering
    }

    override fun unregister() {
        // This module does not need to do something on unregistering
    }
}