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

package catmoe.fallencrystal.moefilter.common.config

import catmoe.fallencrystal.moefilter.common.check.proxy.ProxyCache
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
import catmoe.fallencrystal.moefilter.network.limbo.handler.LimboLoader
import catmoe.fallencrystal.moefilter.network.limbo.handler.ping.CacheMotdManager
import catmoe.fallencrystal.moefilter.network.limbo.listener.LimboListener
import catmoe.fallencrystal.moefilter.util.message.notification.Notifications
import catmoe.fallencrystal.moefilter.util.plugin.LoadCommand
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
        LimboLoader,
        CacheMotdManager,
        BrandCheck,
        LimboListener,
        TrafficManager,
        LoadCommand(),
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
