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
import catmoe.fallencrystal.moefilter.network.common.kick.DisconnectType.*
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
            DomainCheck::class to INVALID_HOST,
            CountryCheck::class to COUNTRY,
            ProxyCheck::class to PROXY,
            ValidNameCheck::class to INVALID_NAME,
            SimilarityCheck::class to INVALID_NAME,
        ).forEach { reason.put(it.key, it.value) }
        addressChecks.addAll(listOf(DomainCheck.instance, CountryCheck(), ProxyCheck()))
    }

    override fun received(packet: LimboPacket, handler: LimboHandler, cancelledRead: Boolean): Boolean {
        if (cancelledRead) return true
        val inetAddress = (handler.address as InetSocketAddress).address
        when (packet) {
            is PacketStatusRequest -> MixedCheck.increase(Pinging(inetAddress, handler.version!!.number))
            is PacketHandshake -> return checkProtocol(packet, handler)
            is PacketInitLogin -> return checkJoin(Joining(packet.username, inetAddress, handler.version!!.number), handler)
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

    private fun kick(handler: LimboHandler, check: AbstractCheck) {
        kick(handler, reason.getIfPresent(check::class) ?: return)
    }

    override fun send(packet: LimboPacket, handler: LimboHandler, cancelled: Boolean): Boolean {
        return if (packet is PacketDisconnect) false
        else this.cancelled.getIfPresent(handler) != null
    }

    private fun checkJoin(joining: Joining, handler: LimboHandler): Boolean {
        val a = ValidNameCheck.instance
        if (a.increase(joining)) { kick(handler, a); return true }
        val mixinKick = MixedCheck.increase(joining)
        if (mixinKick != null) { kick(handler, mixinKick); return true }
        val b = SimilarityCheck.instance
        if (b.increase(joining)) { kick(handler, b); return true }
        //for (i in joinCheck) { if (i.increase(joining)) { kick(handler, reason.getIfPresent(i::class)!!); return true } }
        if (onlineUser.getIfPresent(joining.username.lowercase()) == true || AlreadyOnlineCheck().increase(joining)) {
            kick(handler, ALREADY_ONLINE); return true
        }
        if (!ProtocolConstants.SUPPORTED_VERSION_IDS.contains(joining.protocol)) {
            val kick = PacketDisconnect()
            kick.setReason(ComponentUtil.parse("<red>Unsupported version"))
            handler.channel.write(kick); handler.channel.close(); return true
        }
        return false
    }

    private fun checkProtocol(packet: PacketHandshake, handler: LimboHandler): Boolean {
        if (packet.nextState == Protocol.LOGIN) {
            val addressCheck = Address(handler.address as InetSocketAddress, InetSocketAddress(packet.host, packet.port))
            for (i in addressChecks) {
                if (i.increase(addressCheck)) { kick(handler, reason.getIfPresent(i::class)!!); return true }
            }
            // Limbo online check (address)
            if (onlineAddress.getIfPresent(addressCheck.address.address) != null) { kick(handler, ALREADY_ONLINE); return true }
        }
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