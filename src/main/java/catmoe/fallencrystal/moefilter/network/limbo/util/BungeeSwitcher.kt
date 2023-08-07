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

import catmoe.fallencrystal.moefilter.api.event.EventListener
import catmoe.fallencrystal.moefilter.api.event.FilterEvent
import catmoe.fallencrystal.moefilter.api.event.events.LimboCheckPassedEvent
import catmoe.fallencrystal.moefilter.api.event.events.PluginReloadEvent
import catmoe.fallencrystal.moefilter.common.check.info.CheckInfo
import catmoe.fallencrystal.moefilter.common.check.info.impl.Joining
import catmoe.fallencrystal.moefilter.common.config.LocalConfig
import com.github.benmanes.caffeine.cache.Caffeine
import java.net.InetAddress
import java.util.concurrent.TimeUnit

object BungeeSwitcher : EventListener {

    private var conf = LocalConfig.getLimbo()
    private val limbo = conf.getBoolean("enabled")
    private var timeout = conf.getLong("bungee-queue")
    private var bungeeQueue = Caffeine.newBuilder()
        .expireAfterWrite(timeout, TimeUnit.SECONDS)
        .build<InetAddress, VerifyInfo>()
    private val foreverQueue = Caffeine.newBuilder().build<InetAddress, VerifyInfo>()
    private var alwaysCheck = conf.getBoolean("always-check")

    @FilterEvent
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
    }

    @FilterEvent
    fun passed(event: LimboCheckPassedEvent) {
        val info = VerifyInfo(event.username, event.version)
        bungeeQueue.put(event.address, info)
        if (!alwaysCheck) foreverQueue.put(event.address, info)
    }

    fun connectToBungee(address: InetAddress): Boolean {
        return if (limbo) bungeeQueue.getIfPresent(address) != null
        else if (!alwaysCheck) foreverQueue.getIfPresent(address) != null else true
    }

    fun verify(info: CheckInfo): Boolean {
        if (!limbo) return false
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