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

package catmoe.fallencrystal.moefilter.network.limbo.check.move

import catmoe.fallencrystal.moefilter.network.common.kick.DisconnectType
import catmoe.fallencrystal.moefilter.network.common.kick.FastDisconnect
import catmoe.fallencrystal.moefilter.network.limbo.LimboLocation
import catmoe.fallencrystal.moefilter.network.limbo.block.Block
import catmoe.fallencrystal.moefilter.network.limbo.check.AntiBotChecker
import catmoe.fallencrystal.moefilter.network.limbo.check.LimboCheckType
import catmoe.fallencrystal.moefilter.network.limbo.check.LimboChecker
import catmoe.fallencrystal.moefilter.network.limbo.handler.LimboHandler
import catmoe.fallencrystal.moefilter.network.limbo.handler.LimboLoader
import catmoe.fallencrystal.moefilter.network.limbo.listener.ListenPacket
import catmoe.fallencrystal.moefilter.network.limbo.packet.LimboPacket
import catmoe.fallencrystal.moefilter.network.limbo.packet.c2s.PacketClientPosition
import catmoe.fallencrystal.moefilter.network.limbo.packet.c2s.PacketClientPositionLook
import catmoe.fallencrystal.moefilter.network.limbo.packet.common.Disconnect
import catmoe.fallencrystal.moefilter.network.limbo.packet.s2c.PacketServerPositionLook
import catmoe.fallencrystal.moefilter.network.limbo.util.LimboCheckPassedEvent
import catmoe.fallencrystal.translation.event.EventManager
import catmoe.fallencrystal.translation.utils.config.LocalConfig
import com.github.benmanes.caffeine.cache.Caffeine
import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit
import kotlin.random.Random

@AntiBotChecker(LimboCheckType.HIT_PLATFORM_CHECK)
@ListenPacket(
    PacketClientPosition::class,
    PacketClientPositionLook::class,
    Disconnect::class
)
object HitPlatformChecker : LimboChecker {

    private var config = LocalConfig.getLimbo().getConfig("check.platform-hit")
    private val random = Random
    private val data = Caffeine.newBuilder().build<LimboHandler, TempData>()
    private var block = try { Block.valueOf(config.getAnyRef("block").toString()) } catch (_: Exception) { Block.STONE }
    private var heightMax = config.getInt("platform-height.max")
    private var heightMin = config.getInt("platform-height.min")
    private var roundMax = config.getInt("check-round.max")
    private var roundMin = config.getInt("check-round.min")
    private var spawnHeightMax = config.getInt("spawn-height.max")
    private var spawnHeightMin = config.getInt("spawn-height.min")
    private val loc = LimboLocation(7.5, LimboLoader.spawnHeight, 7.5, 90f, 10f, false)

    override fun reload() {
        config = LocalConfig.getLimbo().getConfig("check.platform-hit")
        try { block=Block.valueOf(config.getAnyRef("block").toString()) } catch (_: Exception) {}
        heightMax=config.getInt("platform-height.max")
        heightMin=config.getInt("platform-height.min")
        roundMax=config.getInt("check-round.max")
        roundMax=config.getInt("check-round.min")
        spawnHeightMax=config.getInt("spawn-height.max")
        spawnHeightMin=config.getInt("spawn-height.min")
    }

    override fun received(packet: LimboPacket, handler: LimboHandler, cancelledRead: Boolean): Boolean {
        if (cancelledRead) return true
        var position: LimboLocation? = null
        val data = this.data.getIfPresent(handler) ?: TempData()
        if (data.passed) return false
        when (packet) {
            is Disconnect -> {
                this.data.invalidate(handler)
                return false
            }
            is PacketClientPosition -> position=packet.loc
            is PacketClientPositionLook -> position=packet.loc
        }
        val platformHeight = data.platformY
        if (platformHeight == null) {
            sendTest(handler, data)
            return false
        }
        val validHeight = platformHeight + block.obj.height()
        if (position != null) {
            if (position.y < validHeight) {
                data.passOne ?: kick(handler)
            } else if (position.y > platformHeight && !position.onGround) data.passOne=null
            if (position.onGround && data.passOne != true) {
                val isValid = validHeight == position.y
                if (!isValid) { kick(handler); return false }
                val round = data.round ?: random.nextInt(roundMin, roundMax + 1)
                val count = data.count ?: 1
                if (count >= round) {
                    pass(handler)
                    handler.sendTestPlatform(platformHeight, Block.AIR.obj)
                    handler.channel.flush()
                } else {
                    data.round=round
                    data.count=count + 1
                    sendTest(handler, data)
                }
                data.passOne=true
            }
        }
        this.data.put(handler, data)
        return false
    }

    private fun sendTest(handler: LimboHandler, data: TempData) {
        val height = random.nextInt(heightMin, heightMax + 1)
        handler.writePacket(PacketServerPositionLook(
            LimboLocation(loc.x, random.nextInt(spawnHeightMin, spawnHeightMax+1).toDouble(), loc.z, loc.yaw, loc.pitch, loc.onGround),
            random.nextInt(0, 32767)
        ))
        val oldY = data.platformY
        if (oldY != null && oldY != height) handler.sendTestPlatform(oldY, Block.AIR.obj)
        handler.sendTestPlatform(height, block.obj)
        data.platformY=height
        handler.channel.flush()
        this.data.put(handler, data)
    }

    private fun kick(a: LimboHandler) = FastDisconnect.disconnect(a, DisconnectType.DETECTED)

    private fun pass(a: LimboHandler) {
        val c = LocalConfig.getLimbo().getConfig("check.timer")
        val t = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - MoveTimer.firstTime.getIfPresent(a)!!)
        if (t < c.getInt("min") || t > c.getInt("max")) {
            kick(a)
            if (LimboLoader.debug) { try { throw Throwable() } catch (e: Throwable) { e.printStackTrace() } }
            return
        }
        // cancel kick.
        data.getIfPresent(a)?.passed = true
        EventManager.callEvent(LimboCheckPassedEvent(
            a.version!!,
            a.profile.username!!,
            (a.address as InetSocketAddress).address, a
        ))
        return
    }

    override fun send(packet: LimboPacket, handler: LimboHandler, cancelled: Boolean) = false

    override fun register() {/* Ignored */}

    override fun unregister() {/* Ignored */}

    internal data class TempData(var platformY: Int? = null, var round: Int? = null, var count: Int? = null, var passOne: Boolean? = null, var passed: Boolean = false)
}