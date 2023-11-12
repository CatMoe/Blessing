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
import catmoe.fallencrystal.moefilter.network.limbo.listener.HandlePacket
import catmoe.fallencrystal.moefilter.network.limbo.packet.LimboPacket
import catmoe.fallencrystal.moefilter.network.limbo.packet.common.PacketKeepAlive
import catmoe.fallencrystal.moefilter.util.plugin.util.Scheduler
import catmoe.fallencrystal.translation.utils.config.LocalConfig
import java.util.concurrent.TimeUnit

@AntiBotChecker(LimboCheckType.KEEP_ALIVE_TIMEOUT)
@HandlePacket(PacketKeepAlive::class)
object KeepAliveCheck : LimboChecker {

    private val queue: MutableCollection<LimboHandler> = ArrayList()
    private val scheduler = Scheduler.getDefault()

    private var timeout = LocalConfig.getLimbo().getLong("keep-alive.max-response")

    override fun reload() {
        timeout = LocalConfig.getLimbo().getLong("keep-alive.max-response")
    }

    override fun received(packet: LimboPacket, handler: LimboHandler, cancelledRead: Boolean): Boolean {
        if (queue.contains(handler)) queue.remove(handler) else kick(handler)
        return false
    }

    override fun send(packet: LimboPacket, handler: LimboHandler, cancelled: Boolean): Boolean {
        if (!cancelled) queue.add(handler)
        scheduler.delayScheduler(timeout, TimeUnit.MILLISECONDS) {
            if (queue.contains(handler) && handler.channel.isActive) kick(handler) else queue.remove(handler)
        }
        return false
    }

    private fun kick(handler: LimboHandler) {
        FastDisconnect.disconnect(handler, DisconnectType.UNEXPECTED_PING)
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