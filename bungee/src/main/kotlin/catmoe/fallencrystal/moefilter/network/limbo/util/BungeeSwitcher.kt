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

package catmoe.fallencrystal.moefilter.network.limbo.util

import catmoe.fallencrystal.moefilter.check.info.CheckInfo
import catmoe.fallencrystal.moefilter.check.info.impl.Joining
import catmoe.fallencrystal.moefilter.common.state.StateManager
import catmoe.fallencrystal.moefilter.event.LimboCheckPassedEvent
import catmoe.fallencrystal.moefilter.event.PluginReloadEvent
import catmoe.fallencrystal.moefilter.network.bungee.util.WorkingMode
import catmoe.fallencrystal.moefilter.util.plugin.AsyncLoader
import catmoe.fallencrystal.translation.event.EventListener
import catmoe.fallencrystal.translation.event.annotations.EventHandler
import catmoe.fallencrystal.translation.event.annotations.HandlerPriority
import catmoe.fallencrystal.translation.utils.config.LocalConfig
import catmoe.fallencrystal.translation.utils.version.Version
import com.github.benmanes.caffeine.cache.Caffeine
import java.net.InetAddress
import java.util.concurrent.TimeUnit

object BungeeSwitcher : EventListener {

    private var conf = LocalConfig.getLimbo()
    val limbo = AsyncLoader.instance.mode == WorkingMode.HANDLE
    private var timeout = conf.getLong("bungee-queue")
    private var bungeeQueue = Caffeine.newBuilder()
        .expireAfterWrite(timeout, TimeUnit.SECONDS)
        .build<InetAddress, VerifyInfo>()
    private val foreverQueue = Caffeine.newBuilder().build<InetAddress, VerifyInfo>()
    private var alwaysCheck = conf.getBoolean("always-check")
    private var connectDuringAttack = conf.getBoolean("only-connect-during-attack")

    @EventHandler(PluginReloadEvent::class, priority = HandlerPriority.HIGH)
    fun reload(event: PluginReloadEvent) {
        if (event.executor == null || !limbo) return
        val conf = LocalConfig.getLimbo()
        val timeout = conf.getLong("bungee-queue")
        if (this.timeout == timeout) return
        this.timeout = timeout
        bungeeQueue.invalidateAll()
        bungeeQueue = Caffeine.newBuilder()
            .expireAfterWrite(BungeeSwitcher.timeout, TimeUnit.SECONDS)
            .build()
        alwaysCheck = conf.getBoolean("always-check")
        this.conf = conf
        connectDuringAttack = BungeeSwitcher.conf.getBoolean("only-connect-during-attack")
    }

    @EventHandler(LimboCheckPassedEvent::class, priority = HandlerPriority.HIGHEST)
    fun passed(event: LimboCheckPassedEvent) {
        val info = VerifyInfo(event.username, event.version)
        bungeeQueue.put(event.address, info)
        if (!alwaysCheck) foreverQueue.put(event.address, info)
    }

    fun connectToBungee(address: InetAddress): Boolean {
        /*
        return if (limbo) bungeeQueue.getIfPresent(address) != null
        else if (!alwaysCheck) foreverQueue.getIfPresent(address) != null else true
         */
        if (!limbo) return true
        var connect = false
        if (connectDuringAttack) connect = !StateManager.inAttack.get()
        val isAllowed = (bungeeQueue.getIfPresent(address) != null)
                || (if (!alwaysCheck) foreverQueue.getIfPresent(address) != null else false)
        if (isAllowed && !connect) {
            connect = true
        }
        return connect
    }

    fun verify(info: CheckInfo): Boolean {
        if (!limbo || !(StateManager.inAttack.get() && connectDuringAttack)) return true
        info as Joining
        val a = bungeeQueue.getIfPresent(info.address) ?: if (!alwaysCheck) (foreverQueue.getIfPresent(info.address)
            ?: return false) else return false
        val result = a.username == info.username && a.version.number == info.protocol
        if (!result) {
            bungeeQueue.invalidate(info.address); foreverQueue.invalidate(info.address)
        }
        return result
    }

    internal class VerifyInfo(
        val username: String,
        val version: Version
    )

}