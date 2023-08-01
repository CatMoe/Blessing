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

import catmoe.fallencrystal.moefilter.network.limbo.check.Checker
import catmoe.fallencrystal.moefilter.network.limbo.check.LimboCheckType
import catmoe.fallencrystal.moefilter.network.limbo.check.LimboChecker
import catmoe.fallencrystal.moefilter.network.limbo.handler.LimboHandler
import catmoe.fallencrystal.moefilter.network.limbo.listener.ILimboListener
import catmoe.fallencrystal.moefilter.network.limbo.listener.IListenerPacket
import catmoe.fallencrystal.moefilter.network.limbo.listener.LimboListener
import catmoe.fallencrystal.moefilter.network.limbo.packet.LimboPacket
import catmoe.fallencrystal.moefilter.network.limbo.packet.common.PacketKeepAlive
import com.github.benmanes.caffeine.cache.Caffeine
import java.util.concurrent.TimeUnit

@Checker(LimboCheckType.UNEXPECTED_KEEPALIVE)
@IListenerPacket(PacketKeepAlive::class)
@Suppress("unused")
object UnexpectedKeepAlive : LimboChecker, ILimboListener {

    private val detectorCache = Caffeine.newBuilder()
        .expireAfterWrite(350, TimeUnit.MILLISECONDS)
        .build<LimboHandler, Boolean>()

    init {
        LimboListener.register(this)
    }

    fun check(handler: LimboHandler) {
        handler.sendPacket(handler.keepAlive)
        handler.sendPacket(handler.keepAlive)
    }

    override fun received(packet: LimboPacket, handler: LimboHandler) {
        if (packet !is PacketKeepAlive) return
        val h = detectorCache.getIfPresent(handler)
        if (h == null) { detectorCache.put(handler, true) } else {
            TODO("Waiting to make a kick packet")
        }
    }

    override fun send(packet: LimboPacket, handler: LimboHandler): Boolean { return false }

}