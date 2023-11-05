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

package catmoe.fallencrystal.moefilter.network.limbo.check.falling

import catmoe.fallencrystal.moefilter.event.LimboCheckPassedEvent
import catmoe.fallencrystal.moefilter.network.common.kick.DisconnectType
import catmoe.fallencrystal.moefilter.network.common.kick.FastDisconnect
import catmoe.fallencrystal.moefilter.network.limbo.check.Checker
import catmoe.fallencrystal.moefilter.network.limbo.check.LimboCheckType
import catmoe.fallencrystal.moefilter.network.limbo.check.LimboChecker
import catmoe.fallencrystal.moefilter.network.limbo.check.falling.FallingCalculateType.COUNT
import catmoe.fallencrystal.moefilter.network.limbo.check.falling.FallingCalculateType.DISTANCE
import catmoe.fallencrystal.moefilter.network.limbo.handler.LimboHandler
import catmoe.fallencrystal.moefilter.network.limbo.handler.MoeLimbo
import catmoe.fallencrystal.moefilter.network.limbo.listener.HandlePacket
import catmoe.fallencrystal.moefilter.network.limbo.packet.LimboPacket
import catmoe.fallencrystal.moefilter.network.limbo.packet.c2s.PacketClientPosition
import catmoe.fallencrystal.moefilter.network.limbo.packet.c2s.PacketClientPositionLook
import catmoe.fallencrystal.moefilter.network.limbo.packet.common.Disconnect
import catmoe.fallencrystal.moefilter.network.limbo.packet.common.PacketPluginMessage
import catmoe.fallencrystal.moefilter.network.limbo.packet.s2c.PacketJoinGame
import catmoe.fallencrystal.moefilter.network.limbo.LimboLocation
import catmoe.fallencrystal.translation.event.EventManager
import catmoe.fallencrystal.translation.utils.config.LocalConfig
import catmoe.fallencrystal.translation.utils.version.Version
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.RemovalCause
import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit
import kotlin.math.abs

@Checker(LimboCheckType.FALLING_CHECK)
@HandlePacket(
    PacketJoinGame::class,
    PacketPluginMessage::class,
    PacketClientPosition::class,
    PacketClientPositionLook::class,
    Disconnect::class
)
object MoveCheck: LimboChecker {

    private val a = Caffeine.newBuilder().build<LimboHandler, LimboLocation>()
    private val b = Caffeine.newBuilder().build<LimboHandler, LimboLocation>()

    private var c = LocalConfig.getLimbo().getConfig("check.falling")
    private var d = c.getDouble("max-x")
    private var e = c.getDouble("max-y")
    private var f = c.getDouble("max-z")
    private var g = c.getBoolean("wrong-turn")
    private var h = c.getBoolean("invalid-y-up")
    private var i = c.getBoolean("invalid-on-ground")
    private var j = c.getBoolean("same-y-position")
    private var k = try { FallingCalculateType.valueOf(c.getAnyRef("calculate-type").toString()) }
    catch (_: IllegalArgumentException) { DISTANCE }

    private var l = c.getLong("timeout")
    private var m = Caffeine.newBuilder()
        .expireAfterWrite(l, TimeUnit.SECONDS)
        .removalListener { handler: LimboHandler?, _: Boolean?, removalCause: RemovalCause? ->
            if (removalCause != RemovalCause.EXPIRED || handler == null) return@removalListener
            k(handler)
        }
        .build<LimboHandler, Boolean>()

    private var o = c.getInt("range")
    private val p = Caffeine.newBuilder().build<LimboHandler, Int>()
    private val q = Caffeine.newBuilder().build<LimboHandler, String>()

    override fun reload() {
        c = LocalConfig.getLimbo().getConfig("check.falling")
        d = c.getDouble("max-x")
        e = c.getDouble("max-y")
        f = c.getDouble("max-z")
        g = c.getBoolean("wrong-turn")
        h = c.getBoolean("invalid-y-up")
        i = c.getBoolean("invalid-on-ground")
        j = c.getBoolean("same-y-position")
        k = try { FallingCalculateType.valueOf(c.getAnyRef("calculate-type").toString()) }
        catch (_: IllegalArgumentException) { DISTANCE }
        val timeout = c.getLong("timeout")
        if (this.l != timeout) {
            MoeLimbo.connections.forEach { k(it) }
            m = Caffeine.newBuilder()
                .expireAfterWrite(timeout, TimeUnit.SECONDS)
                .removalListener { handler: LimboHandler?, _: Boolean?, removalCause: RemovalCause? ->
                    if (removalCause != RemovalCause.EXPIRED || handler == null) return@removalListener
                    k(handler)
                }
                .build()
        }
    }

    override fun received(packet: LimboPacket, handler: LimboHandler, cancelledRead: Boolean): Boolean {
        if (cancelledRead) return true
        if (packet is Disconnect) {
            for (a in listOf(this.a, m, p, b)) a.invalidate(handler)
            return false
        }
        if (packet is PacketPluginMessage && handler.version!!.moreOrEqual(Version.V1_8)) {
            if (packet.channel == "MC|Brand" || packet.channel == "minecraft:brand") q.put(handler, packet.message)
            return false
        }
        m.put(handler, true)
        m.put(handler, true)
        if (packet is PacketJoinGame) return false
        if (q.getIfPresent(handler) == null && handler.version!!.moreOrEqual(Version.V1_8)) { k(handler); return true }
        val a = a.getIfPresent(handler)
        var b: Double? = null
        var c: Double? = null
        var d: Double? = null
        var e: Float? = null
        var f: Float? = null
        var g: Boolean? = null
        val o = p.getIfPresent(handler) ?: 0
        if (packet is PacketClientPosition) {
            val h = packet.lastLoc!!
            b=h.x; c=h.y; d=h.z; g=h.onGround
        }
        if (packet is PacketClientPositionLook) {
            val h = packet.readLoc!!
            b=h.x; c=h.y; d=h.z; e=h.yaw; f=h.pitch; g=h.onGround
            /*
            Check duplicated rotations
             */
            if ((a != null && a.yaw != 0f && a.pitch != 0f && o >= 3) && (a.yaw == h.yaw && a.pitch == h.pitch)) k(handler)
        }
        this.a.put(handler, LimboLocation(b ?: 0.0, c ?: 0.0, d ?: 0.0, e ?: 0f, f ?: 0f, g ?: false))

        // Wrong rotations turn check
        if (f != null && this.g && abs(f) > 90) { k(handler); return true }

        // Y-axis cannot more than last y.
        if ((a?.y ?: c!!) < c!! && h) { k(handler); return true }

        // Valid position check (See isInvalid method)
        for (i in listOf(b, c, d, e?.toDouble(), f?.toDouble())) { if (i(i)) { k(handler); return true } }

        // OnGround cannot be true.
        if (g == true && this.i) {
            if (handler.version == Version.V1_7_6) {
                if (o >= 3) { k(handler); return true }
            } else { k(handler); return true }
        }

        if (a != null) {

            /*
            SameY detector
             */
            if (a.y == c && j && o >= 3) { k(handler); return true }

            /*
            Calculate move distance
             */
            val h = c(a.x, b!!)
            val i = c(a.y, c)
            val j = c(a.z, d!!)
            val k = this.b.getIfPresent(handler)
            if (h > this.d || i > this.e || j > this.f) { k(handler); return true }
            val l = this.c(h, k?.x ?: h)
            val m = this.c(i, k?.y ?: i)
            val n = this.c(j, k?.z ?: j)
            MoeLimbo.debug(handler, "X: $b(C: $h M: ${k?.x} A: $l) Y: $c(C: $i M: ${k?.y} A: $m) Z: $d(C: $j M: ${k?.z} A: $n) C=$o")
            this.b.put(handler, LimboLocation(a(k?.x, h), a(k?.y, i), a(k?.z, j), 0f, 0f, false))
            if (k != null) {
                if (k.x == h && h != 0.0) MoeLimbo.debug(handler, "Final max x speed: $h")
                if (k.y == i && i != 0.0) MoeLimbo.debug(handler, "Final max y speed: $i")
                if (k.z == j && j != 0.0) MoeLimbo.debug(handler, "Final max z speed: $j")
            }
            if (!this.s(m, o)) { k(handler); return true }
        }
        p.put(handler, o+1)
        return when (k) {
            DISTANCE -> { if ((450 - c) >= this.o) { this.d(handler) } else false }
            COUNT ->  { if (o >= this.o) { this.d(handler) } else false }
        }
    }

    private fun a(a: Double?, b: Double): Double { return if (a == null || b >= a) b else a }

    private fun c(a: Double, b: Double): Double {
        if (a == b) return 0.0
        return if (a > b) a - b else b - a
    }

    private fun k(a: LimboHandler) {
        FastDisconnect.disconnect(a, DisconnectType.DETECTED)
        if (MoeLimbo.debug) { try { throw Throwable() } catch (b: Throwable) { b.printStackTrace() } }
    }

    private fun d(a: LimboHandler): Boolean {
        val c = LocalConfig.getLimbo().getConfig("check.timer")
        val t = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - MoveTimer.firstTime.getIfPresent(a)!!)
        if (t < c.getInt("min") || t > c.getInt("max")) { k(a); return false }
        FastDisconnect.disconnect(a, DisconnectType.PASSED_CHECK)
        //EventManager.triggerEvent(LimboCheckPassedEvent(a.version!!, a.profile.username!!, (a.address as InetSocketAddress).address))
        EventManager.callEvent(LimboCheckPassedEvent(a.version!!, a.profile.username!!, (a.address as InetSocketAddress).address))
        return true
    }

    private const val BORDER = 2.9999999E7

    private fun i(a: Double?): Boolean {
        val b = a ?: 0.0
        return b > BORDER || b.isNaN() || b.isInfinite()
    }

    private fun s(a: Double, b: Int): Boolean {
        val t = simulation.getIfPresent(b) ?: 0.0
        if (b < 2) return true
        return a >= t
    }

    override fun send(packet: LimboPacket, handler: LimboHandler, cancelled: Boolean): Boolean { return false }

    private val simulation = Caffeine.newBuilder().build<Int, Double>()

    override fun register() {
        mapOf(
            3..7 to 0.07,
            8..15 to 0.06,
            16..24 to 0.05,
            25..35 to 0.04,
            36..49 to 0.03,
            50..69 to 0.02,
            70..103 to 0.01,
        ).forEach { it.key.forEach { h -> simulation.put(h, it.value) } }
    }

    override fun unregister() { /* Ignored */ }
}