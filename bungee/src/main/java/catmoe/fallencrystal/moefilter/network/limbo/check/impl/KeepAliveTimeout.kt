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

import catmoe.fallencrystal.moefilter.MoeFilter
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
import catmoe.fallencrystal.moefilter.network.limbo.packet.common.PacketKeepAlive
import catmoe.fallencrystal.moefilter.util.plugin.util.Scheduler
import java.util.concurrent.TimeUnit

@Checker(LimboCheckType.KEEP_ALIVE_TIMEOUT)
@HandlePacket(PacketKeepAlive::class)
object KeepAliveTimeout : LimboChecker, ILimboListener {

    private val queue: MutableCollection<LimboHandler> = ArrayList()
    private val scheduler = Scheduler(MoeFilter.instance)

    override fun received(packet: LimboPacket, handler: LimboHandler, cancelledRead: Boolean): Boolean {
        if (queue.contains(handler)) queue.remove(handler) else kick(handler)
        return false
    }

    override fun send(packet: LimboPacket, handler: LimboHandler, cancelled: Boolean): Boolean {
        val h = handler
        if (!cancelled) queue.add(h)
        scheduler.delayScheduler(LocalConfig.getLimbo().getLong("keep-alive.max-response"), TimeUnit.MILLISECONDS) {
            if (queue.contains(h) && handler.channel.isActive) kick(h) else queue.remove(h)
        }
        return false
    }

    private fun kick(handler: LimboHandler) {
        FastDisconnect.disconnect(handler.channel, DisconnectType.UNEXPECTED_PING, ServerKickType.MOELIMBO)
    }


}