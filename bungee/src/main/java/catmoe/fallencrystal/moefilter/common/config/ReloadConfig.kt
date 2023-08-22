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

import catmoe.fallencrystal.moefilter.api.event.EventListener
import catmoe.fallencrystal.moefilter.api.event.FilterEvent
import catmoe.fallencrystal.moefilter.api.event.events.PluginReloadEvent
import catmoe.fallencrystal.moefilter.api.proxy.ProxyCache
import catmoe.fallencrystal.moefilter.check.brand.BrandCheck
import catmoe.fallencrystal.moefilter.common.check.misc.DomainCheck
import catmoe.fallencrystal.moefilter.common.check.mixed.MixedCheck
import catmoe.fallencrystal.moefilter.common.check.name.similarity.SimilarityCheck
import catmoe.fallencrystal.moefilter.common.check.name.valid.ValidNameCheck
import catmoe.fallencrystal.moefilter.common.firewall.Firewall
import catmoe.fallencrystal.moefilter.common.firewall.Throttler
import catmoe.fallencrystal.moefilter.common.geoip.GeoIPManager
import catmoe.fallencrystal.moefilter.network.common.ExceptionCatcher
import catmoe.fallencrystal.moefilter.network.common.kick.FastDisconnect
import catmoe.fallencrystal.moefilter.network.limbo.check.falling.MoveCheck
import catmoe.fallencrystal.moefilter.network.limbo.handler.MoeLimbo
import catmoe.fallencrystal.moefilter.network.limbo.handler.PingManager
import catmoe.fallencrystal.moefilter.util.message.notification.Notifications
import catmoe.fallencrystal.moefilter.util.message.v2.MessageUtil
import catmoe.fallencrystal.moefilter.util.message.v2.packet.type.MessagesType
import catmoe.fallencrystal.moefilter.util.plugin.LoadCommand
import catmoe.fallencrystal.translation.utils.config.LoadConfig
import catmoe.fallencrystal.translation.utils.config.LocalConfig

class ReloadConfig : EventListener {
    @FilterEvent
    fun reloadConfig(event: PluginReloadEvent) {
        // Executor is null == Starting plugin.
        // Load can hot load module without "if" syntax.
        val executor = event.executor
        if (executor != null) {
            LoadConfig.instance.loadConfig()
            LocalConfig.reloadConfig()
            LoadCommand().reload()
            warnMessage(event)
            MixedCheck.reload()
            GeoIPManager.reload()
            if (LocalConfig.getLimbo().getBoolean("enabled")) MoeLimbo.reload()
            MoveCheck.reload()
        }
        Firewall.reload()
        ProxyCache.reload()
        Notifications.reload()
        FastDisconnect.initMessages()
        ExceptionCatcher.reload()
        Throttler.reload()
        PingManager.reload()
        BrandCheck.init()
        // Init checks
        try { SimilarityCheck.instance.reload() } catch (safe: UninitializedPropertyAccessException) { SimilarityCheck() }
        try { DomainCheck.instance.init() } catch (safe: UninitializedPropertyAccessException) { DomainCheck().init() }
        try { ValidNameCheck.instance.init() } catch (safe: UninitializedPropertyAccessException) { ValidNameCheck().init() }
    }

    private fun warnMessage(event: PluginReloadEvent) {
        val sender = event.executor ?: return
        val messageConfig = LocalConfig.getMessage()
        val message = "${messageConfig.getString("prefix")}${messageConfig.getString("reload-warn")}"
        MessageUtil.sendMessage(message, MessagesType.CHAT, sender)
    }
}
