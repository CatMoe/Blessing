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

package catmoe.fallencrystal.moefilter.network.common.motd

import catmoe.fallencrystal.moefilter.common.state.StateManager
import catmoe.fallencrystal.moefilter.network.bungee.handler.IncomingPacketHandler
import catmoe.fallencrystal.moefilter.network.common.ByteMessage
import catmoe.fallencrystal.moefilter.network.limbo.handler.LimboHandler
import catmoe.fallencrystal.moefilter.network.limbo.packet.ExplicitPacket
import catmoe.fallencrystal.moefilter.network.limbo.packet.s2c.PacketPingResponse
import catmoe.fallencrystal.moefilter.util.message.v2.MessageUtil
import catmoe.fallencrystal.translation.utils.config.LocalConfig
import catmoe.fallencrystal.translation.utils.config.Reloadable
import catmoe.fallencrystal.translation.utils.version.Version
import com.github.benmanes.caffeine.cache.Caffeine
import net.md_5.bungee.connection.InitialHandler
import java.net.InetAddress
import java.net.InetSocketAddress
import java.util.*
import java.util.concurrent.TimeUnit

object CacheMotdManager : Reloadable {
    private var conf = LocalConfig.getConfig().getConfig("ping.cache")

    private var useStandardDomain = conf.getBoolean("stable-domain-cache")
    private var fullCacheInAttack = conf.getBoolean("full-cache-during-attack")
    private var sendIconOnce = conf.getBoolean("send-icon-once")
    private var cancelSendIconDuringAttack = conf.getBoolean("cancel-send-icon-during-attack")
    private var cacheLifeTime = conf.getLong("max-life-time")
    private var motdCache = Caffeine.newBuilder()
        .expireAfterWrite(cacheLifeTime, TimeUnit.SECONDS)
        .build<String, MutableMap<Version, CachedMotd>>()
    private val onceIconCache = Caffeine.newBuilder()
        .expireAfterWrite(5, TimeUnit.MINUTES)
        .build<InetAddress, Boolean>()
    private var instantCloseAfterPing = LocalConfig.getLimbo().getBoolean("instant-close-after-ping")

    private var domainLimit = conf.getInt("max-domain-cache")
    private val domainList: MutableCollection<String> = ArrayList()
    private var whitelistedDomainList: List<String> = listOf()

    fun a(p1: String, p2: MutableMap<Version, CachedMotd>) {
        if (fullCacheInAttack && StateManager.inAttack.get()) motdCache.put(p1, p2)
    }

    override fun reload() {
        conf = LocalConfig.getConfig().getConfig("ping.cache")
        useStandardDomain = conf.getBoolean("stable-domain-cache")
        fullCacheInAttack = conf.getBoolean("full-cache-during-attack")
        sendIconOnce = conf.getBoolean("send-icon-once")
        cancelSendIconDuringAttack = conf.getBoolean("cancel-send-icon-during-attack")
        val cacheLifeTime = conf.getLong("max-life-time")
        if (CacheMotdManager.cacheLifeTime != cacheLifeTime) {
            CacheMotdManager.cacheLifeTime =cacheLifeTime
        }
        motdCache = Caffeine.newBuilder()
            .expireAfterWrite(cacheLifeTime, TimeUnit.SECONDS)
            .build()
        instantCloseAfterPing = LocalConfig.getLimbo().getBoolean("instant-close-after-ping")
        domainLimit = conf.getInt("max-domain-cache")
        domainList.clear()
        whitelistedDomainList = conf.getStringList("whitelisted-domain")
        if (whitelistedDomainList.size > domainLimit) {
            MessageUtil.logWarn("[MoeFilter] [PingManager] whitelisted-domain size is more than domain-limit ($domainLimit). Limit will be auto set to ${whitelistedDomainList.size}")
            domainLimit = whitelistedDomainList.size
        }
        domainList.addAll(whitelistedDomainList)
        onceIconCache.invalidateAll()
    }

    fun handlePing(handler: LimboHandler) {
        val version = handler.version
        //val host = handler.host?.hostString ?: ""
        val host = if (useStandardDomain) handler.host?.hostString ?: "" else ""
        if (!checkHost(host))  { handler.channel.close(); return }
        val cache = motdCache.getIfPresent(host)
        val c = if (cache?.get(version) == null) createMap(handler, cache) else cache
        val address = (handler.address as InetSocketAddress).address
        if (cancelSendIconDuringAttack && StateManager.inAttack.get()) { sendMotd(c[version]!!, handler, true); return }
        if (sendIconOnce) {
            val noIcon = onceIconCache.getIfPresent(address) != null
            if (!noIcon) onceIconCache.put(address, true)
            sendMotd(c[version]!!, handler, noIcon)
        }
        if (fullCacheInAttack && StateManager.inAttack.get()) motdCache.put(host, c)
    }

    fun cachePing(version: Version, host: String, output: String) {
        if (checkHost(host)) {
            val cache = motdCache.getIfPresent(if (useStandardDomain) host else "")
            if (cache?.get(version) == null) createMap(output, version, host, cache)
        }
    }

    fun handlePing(initialHandler: InitialHandler, handler: IncomingPacketHandler): Boolean {
        val channel = handler.channel
        val version = Version.of(initialHandler.version)
        val host = if (useStandardDomain) initialHandler.virtualHost.hostString else ""
        val cache = motdCache.getIfPresent(host)?.get(version) ?: return false
        val byteBuf = ByteMessage.create()
        sendMotd(cache, byteBuf, (cancelSendIconDuringAttack && StateManager.inAttack.get()))
        if (byteBuf.readableBytes() == 0) {
            byteBuf.release()
            return false
        }
        channel.writeAndFlush(byteBuf).addListener { if (instantCloseAfterPing) channel.close() }
        return true
    }

    private fun sendMotd(motd: CachedMotd, handler: LimboHandler, noIcon: Boolean) {
        // handler.writePacket(if (noIcon) packet.bmNoIcon else packet.bm)
        val pa = ExplicitPacket(0x00, (if (noIcon) motd.bytesWithoutIcon else motd.bytes), "Cached ping packet (icon: ${!noIcon})")
        handler.sendPacket(pa)
        if (instantCloseAfterPing) handler.channel.close()
    }

    private fun sendMotd(motd: CachedMotd, byteBuf: ByteMessage, noIcon: Boolean) {
        byteBuf.writeVarInt(0x00)
        byteBuf.writeBytes(if (noIcon) motd.bytesWithoutIcon else motd.bytes)
    }

    private fun createMap(handler: LimboHandler, map: MutableMap<Version, CachedMotd>?) =
        createMap(
            handler.fakeHandler!!.handlePing(handler.host!!, handler.version).description,
            handler.version,
            handler.host?.hostString ?: "",
            map
        )

    private fun createMap(input: String, version: Version, host: String = "", map: MutableMap<Version, CachedMotd>?): MutableMap<Version, CachedMotd> {
        val m = map ?: EnumMap(Version::class.java)
        val i = CachedMotd.process(PacketPingResponse(output = input), version)
        m[version] = i
        motdCache.put(if (useStandardDomain) host else "", m)
        return m
    }


    private fun checkHost(host: String): Boolean {
        return if  (!useStandardDomain) true
        else {
            if (domainList.contains(host)) true else {
                if (domainList.size + 1 > domainLimit) false else { domainList.add(host); true }
            }
        }
    }

}