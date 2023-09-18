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

import catmoe.fallencrystal.moefilter.check.AbstractCheck
import catmoe.fallencrystal.moefilter.check.info.impl.Address
import catmoe.fallencrystal.moefilter.check.info.impl.Joining
import catmoe.fallencrystal.moefilter.check.info.impl.Pinging
import catmoe.fallencrystal.moefilter.common.check.misc.AlreadyOnlineCheck
import catmoe.fallencrystal.moefilter.common.check.misc.CountryCheck
import catmoe.fallencrystal.moefilter.common.check.misc.DomainCheck
import catmoe.fallencrystal.moefilter.common.check.misc.ProxyCheck
import catmoe.fallencrystal.moefilter.common.check.mixed.MixedCheck
import catmoe.fallencrystal.moefilter.common.check.name.similarity.SimilarityCheck
import catmoe.fallencrystal.moefilter.common.check.name.valid.ValidNameCheck
import catmoe.fallencrystal.moefilter.network.common.kick.DisconnectType
import catmoe.fallencrystal.moefilter.network.common.kick.FastDisconnect
import catmoe.fallencrystal.moefilter.network.limbo.check.Checker
import catmoe.fallencrystal.moefilter.network.limbo.check.LimboCheckType
import catmoe.fallencrystal.moefilter.network.limbo.check.LimboChecker
import catmoe.fallencrystal.moefilter.network.limbo.handler.LimboHandler
import catmoe.fallencrystal.moefilter.network.limbo.listener.HandlePacket
import catmoe.fallencrystal.moefilter.network.limbo.packet.LimboPacket
import catmoe.fallencrystal.moefilter.network.limbo.packet.c2s.PacketHandshake
import catmoe.fallencrystal.moefilter.network.limbo.packet.c2s.PacketInitLogin
import catmoe.fallencrystal.moefilter.network.limbo.packet.c2s.PacketStatusRequest
import catmoe.fallencrystal.moefilter.network.limbo.packet.common.Disconnect
import catmoe.fallencrystal.moefilter.network.limbo.packet.protocol.Protocol
import catmoe.fallencrystal.moefilter.network.limbo.packet.s2c.PacketDisconnect
import catmoe.fallencrystal.moefilter.network.limbo.packet.s2c.PacketEmptyChunk
import catmoe.fallencrystal.moefilter.network.limbo.packet.s2c.PacketJoinGame
import catmoe.fallencrystal.translation.utils.component.ComponentUtil
import com.github.benmanes.caffeine.cache.Caffeine
import net.md_5.bungee.protocol.ProtocolConstants
import java.net.InetAddress
import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass

@Checker(LimboCheckType.TRANSLATE_JOIN_CHECK)
@HandlePacket(
    PacketHandshake::class,
    PacketInitLogin::class,
    PacketJoinGame::class,
    PacketStatusRequest::class,
    PacketEmptyChunk::class,
    Disconnect::class
)
object CommonJoinCheck : LimboChecker {

    private val onlineAddress = Caffeine.newBuilder().build<InetAddress, Boolean>()
    private val onlineUser = Caffeine.newBuilder().build<String, Boolean>()
    private val cancelled = Caffeine.newBuilder().expireAfterWrite(250, TimeUnit.MILLISECONDS).build<LimboHandler, Boolean>()

    override fun reload() {
        /*
        This module does not need that.
         */
    }

    private val reason = Caffeine.newBuilder().build<KClass<out AbstractCheck>, DisconnectType>()
    private val addressChecks: MutableCollection<AbstractCheck> = ArrayList()

    init {
        mapOf(
            DomainCheck::class to DisconnectType.INVALID_HOST,
            CountryCheck::class to DisconnectType.COUNTRY,
            ProxyCheck::class to DisconnectType.PROXY,
        ).forEach { reason.put(it.key, it.value) }
        for (it in listOf(DomainCheck.instance, CountryCheck(), ProxyCheck())) addressChecks.add(it)
    }

    override fun received(packet: LimboPacket, handler: LimboHandler, cancelledRead: Boolean): Boolean {
        if (cancelledRead) return true
        val inetSocketAddress = handler.address as InetSocketAddress
        val inetAddress = inetSocketAddress.address
        when (packet) {
            is PacketStatusRequest -> MixedCheck.increase(Pinging(inetAddress, handler.version!!.number))
            is PacketHandshake -> {
                if (packet.nextState == Protocol.LOGIN) {
                    val addressCheck = Address(inetSocketAddress, InetSocketAddress(packet.host, packet.port))
                    for (i in addressChecks) {
                        if (i.increase(addressCheck)) { kick(handler, reason.getIfPresent(i::class)!!) }
                    }
                    // Limbo online check (address)
                    if (onlineAddress.getIfPresent(inetAddress) != null) { kick(handler, DisconnectType.ALREADY_ONLINE); return true }
                }
            }
            is PacketInitLogin -> {
                val username = packet.username
                val joining = Joining(username, inetAddress, handler.version!!.number)
                // Invalid name check
                if (ValidNameCheck.instance.increase(joining)) { kick(handler, DisconnectType.INVALID_NAME); return true }
                // Ping & Join mixin check
                val mixinKick = MixedCheck.increase(joining)
                if (mixinKick != null) { kick(handler, mixinKick); return true }
                // Limbo & Bungee online check (username)
                if (onlineUser.getIfPresent(username.lowercase()) == true || AlreadyOnlineCheck().increase(joining)) {
                    kick(handler, DisconnectType.ALREADY_ONLINE); return true
                }
                // Similarity name check
                if (SimilarityCheck.instance.increase(joining)) { kick(handler, DisconnectType.INVALID_NAME); return true }

                val version = handler.version!!
                if (!version.isSupported || !ProtocolConstants.SUPPORTED_VERSION_IDS.contains(version.number)) {
                    val kick = PacketDisconnect()
                    kick.setReason(ComponentUtil.parse("<red>Unsupported version"))
                    handler.channel.write(kick); handler.channel.close(); return true
                }
            }
            is PacketJoinGame -> {
                onlineAddress.put(inetAddress, true)
                onlineUser.put(handler.profile.username!!.lowercase(), true)
            }
            is Disconnect -> {
                onlineAddress.invalidate(inetAddress)
                onlineUser.invalidate((handler.profile.username ?: return false).lowercase())
            }
        }
        return false
    }

    private fun kick(handler: LimboHandler, type: DisconnectType) {
        cancelled.put(handler, true)
        FastDisconnect.disconnect(handler, type)
    }

    override fun send(packet: LimboPacket, handler: LimboHandler, cancelled: Boolean): Boolean {
        return if (packet is PacketDisconnect) false
        else this.cancelled.getIfPresent(handler) != null
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