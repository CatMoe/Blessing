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

import catmoe.fallencrystal.moefilter.common.counter.ConnectionCounter
import catmoe.fallencrystal.moefilter.common.counter.type.BlockType
import catmoe.fallencrystal.moefilter.common.firewall.Firewall
import catmoe.fallencrystal.moefilter.network.bungee.pipeline.MoeChannelHandler
import catmoe.fallencrystal.moefilter.network.limbo.check.Checker
import catmoe.fallencrystal.moefilter.network.limbo.check.LimboCheckType
import catmoe.fallencrystal.moefilter.network.limbo.check.LimboChecker
import catmoe.fallencrystal.moefilter.network.limbo.handler.LimboHandler
import catmoe.fallencrystal.moefilter.network.limbo.listener.HandlePacket
import catmoe.fallencrystal.moefilter.network.limbo.listener.ILimboListener
import catmoe.fallencrystal.moefilter.network.limbo.packet.LimboPacket
import catmoe.fallencrystal.moefilter.network.limbo.packet.common.Disconnect
import java.net.InetSocketAddress

@Suppress("unused")
@Checker(LimboCheckType.INSTANT_DISCONNECT)
@HandlePacket(Disconnect::class)
object InstantDisconnectCheck : LimboChecker, ILimboListener {
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
            ConnectionCounter.countBlocked(BlockType.FIREWALL)
        }
        return false
    }

    override fun send(packet: LimboPacket, handler: LimboHandler, cancelled: Boolean): Boolean { return false }
}