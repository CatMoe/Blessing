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

package catmoe.fallencrystal.moefilter.network.bungee.handler

import catmoe.fallencrystal.moefilter.check.info.impl.Address
import catmoe.fallencrystal.moefilter.check.info.impl.Joining
import catmoe.fallencrystal.moefilter.check.info.impl.Pinging
import catmoe.fallencrystal.moefilter.check.misc.AlreadyOnlineCheck
import catmoe.fallencrystal.moefilter.common.check.misc.CountryCheck
import catmoe.fallencrystal.moefilter.common.check.misc.DomainCheck
import catmoe.fallencrystal.moefilter.common.check.misc.ProxyCheck
import catmoe.fallencrystal.moefilter.common.check.mixed.MixedCheck
import catmoe.fallencrystal.moefilter.common.check.name.similarity.SimilarityCheck
import catmoe.fallencrystal.moefilter.common.check.name.valid.ValidNameCheck
import catmoe.fallencrystal.moefilter.network.bungee.initializer.IPipeline
import catmoe.fallencrystal.moefilter.network.bungee.util.PlayerChannelRecord
import catmoe.fallencrystal.moefilter.network.common.ExceptionCatcher
import catmoe.fallencrystal.moefilter.network.common.ServerType
import catmoe.fallencrystal.moefilter.network.common.exception.InvalidHandshakeException
import catmoe.fallencrystal.moefilter.network.common.exception.InvalidPacketException
import catmoe.fallencrystal.moefilter.network.common.kick.DisconnectType
import catmoe.fallencrystal.moefilter.network.common.kick.FastDisconnect
import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import net.md_5.bungee.protocol.DefinedPacket
import net.md_5.bungee.protocol.PacketWrapper
import net.md_5.bungee.protocol.packet.Handshake
import net.md_5.bungee.protocol.packet.LoginRequest
import net.md_5.bungee.protocol.packet.PingPacket
import net.md_5.bungee.protocol.packet.StatusRequest
import java.net.InetSocketAddress
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.reflect.KClass

class PacketAntibotHandler(ctx: ChannelHandlerContext) : ChannelDuplexHandler() {

    private var handshake: Handshake? = null
    private val cancelRead = AtomicBoolean(false)
    private var allowUnknown = false
    private var nextPacket = NextPacket.HANDSHAKE
    private val channel = ctx.channel()
    private val address get() = (channel.remoteAddress() as InetSocketAddress)

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any?) {
        if (msg == null && !allowUnknown) {
            cancelRead.set(true)
            channel.close()
            return
        }
        run {
            if (msg is PacketWrapper) {
                when (val packet = msg.packet) {
                    is Handshake -> {
                        nextPacket.verify(packet)
                        this.handshake=packet
                        when (packet.requestedProtocol) {
                            1 -> {
                                MixedCheck.increase(Pinging(address.address, packet.protocolVersion))
                                nextPacket = NextPacket.STATUS_REQUEST
                            }
                            2 -> {
                                val info = Address(address, InetSocketAddress(packet.host, packet.port))
                                when {
                                    DomainCheck.instance.increase(info) -> kick(DisconnectType.INVALID_HOST)
                                    CountryCheck().increase(info) -> kick(DisconnectType.COUNTRY)
                                    ProxyCheck().increase(info) -> kick(DisconnectType.PROXY)
                                }
                                if (cancelRead.get()) return@run
                                nextPacket= NextPacket.LOGIN
                            }
                            else -> throw InvalidHandshakeException("Handshake must be 1 or 2")
                        }
                    }
                    // Ping
                    is StatusRequest -> {
                        nextPacket.verify(packet)
                        nextPacket=NextPacket.STATUS_PING
                    }
                    is PingPacket -> nextPacket.verify(packet)
                    // Join
                    is LoginRequest -> {
                        nextPacket.verify(packet)
                        val join = Joining(packet.data, address.address, handshake!!.protocolVersion)
                        if (ValidNameCheck.instance.increase(join)) { kick(DisconnectType.INVALID_NAME); return@run }
                        val mixedCheck = MixedCheck.increase(join)
                        when {
                            mixedCheck != null -> kick(mixedCheck)
                            SimilarityCheck.instance.increase(join) -> kick(DisconnectType.INVALID_NAME)
                            AlreadyOnlineCheck().increase(join) -> kick(DisconnectType.ALREADY_ONLINE)
                        }
                        if (!cancelRead.get()) {
                            allowUnknown = true
                            val pipeline = channel.pipeline()
                            PlayerChannelRecord.putChannelHandler(ctx, packet.data)
                            val initialHandler = pipeline[AnotherHandlerBoss::class.java].initialHandler ?: return@run
                            pipeline.replace(IPipeline.PACKET_INTERCEPTOR, IPipeline.PACKET_INTERCEPTOR, BasicPacketHandler(packet.data, initialHandler))
                        }
                    }
                    else -> if (!allowUnknown) throw InvalidPacketException("Invalid packet order!")
                }
            }
        }
        if (!cancelRead.get()) super.channelRead(ctx, msg)
    }

    private fun kick(type: DisconnectType) {
        FastDisconnect.disconnect(channel, type, ServerType.BUNGEE_CORD)
        cancelRead.set(true)
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) = ExceptionCatcher.handle(channel, cause)

    internal enum class NextPacket(private val packet: KClass<out DefinedPacket>) {
        HANDSHAKE(Handshake::class),
        LOGIN(LoginRequest::class),
        STATUS_REQUEST(StatusRequest::class),
        STATUS_PING(PingPacket::class);

        fun verify(packet: DefinedPacket) {
            if (this.packet != packet::class) throw InvalidPacketException("Next packet should be ${this.packet.simpleName} but actually is ${packet::class.simpleName}")
        }
    }

}