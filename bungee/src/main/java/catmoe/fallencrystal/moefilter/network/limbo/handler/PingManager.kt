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

package catmoe.fallencrystal.moefilter.network.limbo.handler

import catmoe.fallencrystal.moefilter.common.state.StateManager
import catmoe.fallencrystal.moefilter.network.limbo.netty.ByteMessage
import catmoe.fallencrystal.moefilter.network.limbo.packet.ExplicitPacket
import catmoe.fallencrystal.moefilter.network.limbo.packet.s2c.PacketPingResponse
import catmoe.fallencrystal.translation.utils.config.LocalConfig
import catmoe.fallencrystal.translation.utils.version.Version
import com.github.benmanes.caffeine.cache.Caffeine
import io.netty.buffer.Unpooled
import java.net.InetAddress
import java.net.InetSocketAddress
import java.util.*
import java.util.concurrent.TimeUnit

object PingManager {
    private var conf = LocalConfig.getConfig().getConfig("ping.cache")

    private var useStandardDomain = conf.getBoolean("stable-domain-cache")
    private var protocolAlwaysUnsupported = LocalConfig.getConfig().getBoolean("ping.protocol-always-unsupported")
    private var fullCacheInAttack = conf.getBoolean("full-cache-during-attack")
    private var sendIconOnce = conf.getBoolean("send-icon-once")
    private var cancelSendIconDuringAttack = conf.getBoolean("cancel-send-icon-during-attack")
    private var cacheLifeTime = conf.getLong("max-life-time")
    private var motdCache = Caffeine.newBuilder()
        .expireAfterWrite(cacheLifeTime, TimeUnit.SECONDS)
        .build<String, MutableMap<Version, MotdInfo>>()
    private val onceIconCache = Caffeine.newBuilder()
        .expireAfterWrite(5, TimeUnit.MINUTES)
        .build<InetAddress, Boolean>()
    private var instantCloseAfterPing = LocalConfig.getLimbo().getBoolean("instant-close-after-ping")

    private val dv get() = if (protocolAlwaysUnsupported) Version.V1_8 else null

    fun a(p1: String, p2: MutableMap<Version, MotdInfo>) {
        if (fullCacheInAttack && StateManager.inAttack.get()) motdCache.put(p1, p2)
    }

    fun reload() {
        conf = LocalConfig.getConfig().getConfig("ping.cache")
        useStandardDomain = conf.getBoolean("stable-domain-cache")
        protocolAlwaysUnsupported = LocalConfig.getConfig().getBoolean("ping.protocol-always-unsupported")
        fullCacheInAttack = conf.getBoolean("full-cache-during-attack")
        sendIconOnce = conf.getBoolean("send-icon-once")
        cancelSendIconDuringAttack = conf.getBoolean("cancel-send-icon-during-attack")
        val cacheLifeTime = conf.getLong("max-life-time")
        if (PingManager.cacheLifeTime != cacheLifeTime) {
            PingManager.cacheLifeTime =cacheLifeTime
        }
        motdCache = Caffeine.newBuilder()
            .expireAfterWrite(cacheLifeTime, TimeUnit.SECONDS)
            .build()
        instantCloseAfterPing = LocalConfig.getLimbo().getBoolean("instant-close-after-ping")
    }

    fun handlePing(handler: LimboHandler) {
        val version = handler.version!!
        val i = if (useStandardDomain) handler.host!!.hostString else ""
        val cache = motdCache.getIfPresent(i)
        val c = if (cache?.get(version) == null) createMap(handler, cache) else cache
        val address = (handler.address as InetSocketAddress).address
        if (cancelSendIconDuringAttack && StateManager.inAttack.get()) { sendMotd(c, handler, true); return }
        if (sendIconOnce) {
            val noIcon = onceIconCache.getIfPresent(address) != null
            if (!noIcon) onceIconCache.put(address, true)
            sendMotd(c, handler, noIcon)
        }
        if (fullCacheInAttack && StateManager.inAttack.get()) motdCache.put(i, c)
    }

    private fun sendMotd(map: MutableMap<Version, MotdInfo>, handler: LimboHandler, noIcon: Boolean) {
        val p = map[dv ?: handler.version!!]!!
        // handler.writePacket(if (noIcon) packet.bmNoIcon else packet.bm)
        val pa = ExplicitPacket(0x00, (if (noIcon) p.bmNoIcon else p.bm), "Cached ping packet")
        handler.sendPacket(pa)
        if (instantCloseAfterPing) handler.channel.close()
    }

    private fun createMap(handler: LimboHandler, map: MutableMap<Version, MotdInfo>?): MutableMap<Version, MotdInfo> {
        val m = map ?: EnumMap(Version::class.java)
        val packet = PacketPingResponse()
        packet.output=handler.fakeHandler!!.handlePing(handler.host!!, handler.version!!).description
        val i = MotdInfo(
            packet,
            process(packet, handler.version!!, false),
            process(packet, handler.version!!, true),
            handler.version!!
        )
        m[dv ?: handler.version!!] = i
        motdCache.put(if (useStandardDomain) handler.host!!.hostString else "", m)
        return m
    }

    private fun process(packet: PacketPingResponse, version: Version, noIcon: Boolean): ByteArray {
        val bm = ByteMessage(Unpooled.buffer())
        if (noIcon) packet.output=packet.output!!.replace(""","favicon":"data:(.*?)"""".toRegex(), "")
        MoeLimbo.debug("${packet.output}")
        packet.encode(bm, version)
        val array = bm.toByteArray()
        bm.release()
        return array
    }

}

class MotdInfo(
    val packet: PacketPingResponse,
    val bm: ByteArray,
    val bmNoIcon: ByteArray,
    val version: Version,
)