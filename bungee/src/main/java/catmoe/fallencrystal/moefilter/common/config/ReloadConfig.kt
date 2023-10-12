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

package catmoe.fallencrystal.moefilter.common.config

import catmoe.fallencrystal.moefilter.api.proxy.ProxyCache
import catmoe.fallencrystal.moefilter.check.brand.BrandCheck
import catmoe.fallencrystal.moefilter.common.check.misc.DomainCheck
import catmoe.fallencrystal.moefilter.common.check.mixed.MixedCheck
import catmoe.fallencrystal.moefilter.common.check.name.similarity.SimilarityCheck
import catmoe.fallencrystal.moefilter.common.check.name.valid.ValidNameCheck
import catmoe.fallencrystal.moefilter.common.firewall.Firewall
import catmoe.fallencrystal.moefilter.common.firewall.Throttler
import catmoe.fallencrystal.moefilter.common.geoip.GeoIPManager
import catmoe.fallencrystal.moefilter.event.PluginReloadEvent
import catmoe.fallencrystal.moefilter.network.common.ExceptionCatcher
import catmoe.fallencrystal.moefilter.network.common.kick.FastDisconnect
import catmoe.fallencrystal.moefilter.network.common.traffic.TrafficManager
import catmoe.fallencrystal.moefilter.network.limbo.handler.MoeLimbo
import catmoe.fallencrystal.moefilter.network.limbo.handler.ping.CacheMotdManager
import catmoe.fallencrystal.moefilter.network.limbo.listener.LimboListener
import catmoe.fallencrystal.moefilter.util.message.notification.Notifications
import catmoe.fallencrystal.translation.event.EventListener
import catmoe.fallencrystal.translation.event.annotations.EventHandler
import catmoe.fallencrystal.translation.event.annotations.HandlerPriority
import catmoe.fallencrystal.translation.platform.Platform
import catmoe.fallencrystal.translation.platform.ProxyPlatform
import catmoe.fallencrystal.translation.utils.component.ComponentUtil
import catmoe.fallencrystal.translation.utils.config.IgnoreInitReload
import catmoe.fallencrystal.translation.utils.config.LocalConfig
import catmoe.fallencrystal.translation.utils.config.Reloadable
import java.util.concurrent.CopyOnWriteArrayList

object ReloadConfig : EventListener {

    val reloadable: MutableCollection<Reloadable> = CopyOnWriteArrayList(listOf(
        LocalConfig,
        MixedCheck,
        GeoIPManager,
        Firewall,
        ProxyCache,
        Notifications,
        FastDisconnect,
        ExceptionCatcher,
        Throttler,
        try { SimilarityCheck.instance } catch (safe: UninitializedPropertyAccessException) { SimilarityCheck() },
        try { DomainCheck.instance } catch (safe: UninitializedPropertyAccessException) { DomainCheck() },
        try { ValidNameCheck.instance } catch (safe: UninitializedPropertyAccessException) { ValidNameCheck() },
        MoeLimbo,
        CacheMotdManager,
        BrandCheck,
        LimboListener,
        TrafficManager,
    ))

    @EventHandler(PluginReloadEvent::class, priority = HandlerPriority.HIGHEST)
    @Platform(ProxyPlatform.BUNGEE)
    fun reloadConfig(event: PluginReloadEvent) {
        // Executor is null == Starting plugin.
        // Load can hot load module without "if" syntax.
        val executor = event.executor
        for (reloadable in this.reloadable) {
            if (reloadable::class.java.isAnnotationPresent(IgnoreInitReload::class.java) && executor == null) continue
            try { reloadable.reload() } catch (e: Exception) { e.printStackTrace(); continue }
        }
        warnMessage(event)
    }

    private fun warnMessage(event: PluginReloadEvent) {
        val messageConfig = LocalConfig.getMessage()
        val message = "${messageConfig.getString("prefix")}${messageConfig.getString("reload-warn")}"
        (event.executor ?: return).sendMessage(ComponentUtil.parse(message))
    }
}
