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

package catmoe.fallencrystal.moefilter.network.limbo.util

import catmoe.fallencrystal.moefilter.check.info.CheckInfo
import catmoe.fallencrystal.moefilter.check.info.impl.Joining
import catmoe.fallencrystal.moefilter.common.state.StateManager
import catmoe.fallencrystal.moefilter.event.LimboCheckPassedEvent
import catmoe.fallencrystal.moefilter.event.PluginReloadEvent
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
    val limbo = conf.getBoolean("enabled")
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
        this.timeout=timeout
        bungeeQueue = Caffeine.newBuilder()
            .expireAfterWrite(BungeeSwitcher.timeout, TimeUnit.SECONDS)
            .build()
        alwaysCheck = conf.getBoolean("always-check")
        this.conf=conf
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
        if (connectDuringAttack) connect=!StateManager.inAttack.get()
        val isAllowed = (bungeeQueue.getIfPresent(address) != null)
                || (if (!alwaysCheck) foreverQueue.getIfPresent(address) != null else false)
        if (isAllowed && !connect) { connect=true }
        return connect
    }

    fun verify(info: CheckInfo): Boolean {
        if (!limbo || (StateManager.inAttack.get() && connectDuringAttack)) return true
        info as Joining
        val a = bungeeQueue.getIfPresent(info.address) ?: if (!alwaysCheck) (foreverQueue.getIfPresent(info.address) ?: return false) else return false
        val result = a.username == info.username && a.version.number == info.protocol
        if (!result) { bungeeQueue.invalidate(info.address); foreverQueue.invalidate(info.address) }
        return result
    }

}

class VerifyInfo(
    val username: String,
    val version: Version
)