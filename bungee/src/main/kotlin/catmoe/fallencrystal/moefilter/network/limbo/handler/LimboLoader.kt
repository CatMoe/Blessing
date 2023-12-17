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

package catmoe.fallencrystal.moefilter.network.limbo.handler

import catmoe.fallencrystal.moefilter.common.firewall.lockdown.LockdownManager
import catmoe.fallencrystal.moefilter.network.bungee.initializer.MoeChannelHandler
import catmoe.fallencrystal.moefilter.network.bungee.util.WorkingMode
import catmoe.fallencrystal.moefilter.network.limbo.block.Block
import catmoe.fallencrystal.moefilter.network.limbo.check.impl.ChatCheck
import catmoe.fallencrystal.moefilter.network.limbo.check.impl.CommonJoinCheck
import catmoe.fallencrystal.moefilter.network.limbo.check.impl.KeepAliveCheck
import catmoe.fallencrystal.moefilter.network.limbo.check.impl.TransactionCheck
import catmoe.fallencrystal.moefilter.network.limbo.check.move.HitPlatformChecker
import catmoe.fallencrystal.moefilter.network.limbo.check.move.MoveTimer
import catmoe.fallencrystal.moefilter.network.limbo.check.valid.PacketOrderCheck
import catmoe.fallencrystal.moefilter.network.limbo.compat.message.NbtMessage
import catmoe.fallencrystal.moefilter.network.limbo.dimension.llbit.DimensionType
import catmoe.fallencrystal.moefilter.network.limbo.dimension.llbit.StaticDimension
import catmoe.fallencrystal.moefilter.network.limbo.listener.LimboListener
import catmoe.fallencrystal.moefilter.network.limbo.packet.cache.PacketCache
import catmoe.fallencrystal.moefilter.network.limbo.packet.protocol.Protocol
import catmoe.fallencrystal.moefilter.network.limbo.packet.s2c.PacketServerChat
import catmoe.fallencrystal.moefilter.util.message.v2.MessageUtil
import catmoe.fallencrystal.moefilter.util.plugin.AsyncLoader
import catmoe.fallencrystal.translation.utils.component.ComponentUtil
import catmoe.fallencrystal.translation.utils.config.IgnoreInitReload
import catmoe.fallencrystal.translation.utils.config.LocalConfig
import catmoe.fallencrystal.translation.utils.config.Reloadable

@Suppress("EnumValuesSoftDeprecate")
@IgnoreInitReload
object LimboLoader : Reloadable {

    private var limboConfig = LocalConfig.getLimbo()
    val connections: MutableCollection<LimboHandler> = ArrayList()
    private val dimension = limboConfig.getAnyRef("dimension").toString()
    val dimensionType = DimensionType.OVERWORLD
    var debug = LocalConfig.getConfig().getBoolean("debug")

    // Debug
    private val disableCheck = limboConfig.getBoolean("debug.check.disable-all")
    val reduceDebug = limboConfig.getBoolean("debug.reduce-f3-debug")
    val chunkStart = limboConfig.getInt("debug.chunk.start")
    val chunkLength = chunkStart + limboConfig.getInt("debug.chunk.length")
    val platformSummon = limboConfig.getBoolean("debug.test-platform.summon")
    val platformHeight = limboConfig.getInt("debug.test-platform.y")
    val platformBlock = try {
        Block.valueOf(limboConfig.getAnyRef("debug.test-platform.block").toString()).obj
    } catch (_: Exception) {
        Block.STONE.obj
    }
    val spawnHeight = limboConfig.getDouble("spawn-height")

    val testChatPacket = if (limboConfig.getBoolean("debug.chat.enabled"))
        PacketServerChat(
            type = PacketServerChat.MessageType.valueOf(limboConfig.getString("debug.chat.type")),
            message = NbtMessage.create(ComponentUtil.parse(limboConfig.getString("debug.chat.message")))
        )
    else null

    private val checker = listOf(
        CommonJoinCheck,
        HitPlatformChecker,
        MoveTimer,
        KeepAliveCheck,
        PacketOrderCheck,
        ChatCheck,
        TransactionCheck,
    )

    override fun reload() {
        if (AsyncLoader.instance.mode == WorkingMode.DISABLED) return
        LockdownManager.setLockdown(true)
        calibrateConnections()
        if (!disableCheck) checker.forEach { it.reload() }
        initLimbo()
        LockdownManager.setLockdown(false)
    }

    fun calibrateConnections() {
        val connections: MutableCollection<LimboHandler> = ArrayList()
        this.connections.forEach { try { if (!it.channel.isActive || MoeChannelHandler.sentHandshake.getIfPresent(it.channel) == null) connections.remove(it) } catch (npe: NullPointerException) { connections.add(it) } }
        this.connections.removeAll(connections.toSet())
    }

    fun debug(log: String) {
        if (debug) MessageUtil.logInfo("[MoeLimbo] $log")
    }

    fun debug(handler: LimboHandler?, log: String) {
        val profile = handler?.profile ?: "Unknown"
        this.debug("$profile $log")
    }

    fun initLimbo() {
        limboConfig = LocalConfig.getLimbo()
        StaticDimension.init()
        debug = LocalConfig.getConfig().getBoolean("debug")
        Protocol.values().forEach { Protocol.STATE_BY_ID[it.stateId] = it }
        if (!disableCheck) for (c in checker) LimboListener.register(c)
        PacketCache.initPacket()
    }

}