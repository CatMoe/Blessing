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

package catmoe.fallencrystal.moefilter.common.state

import catmoe.fallencrystal.moefilter.common.counter.ConnectionStatistics
import catmoe.fallencrystal.moefilter.event.AttackStoppedEvent
import catmoe.fallencrystal.moefilter.event.UnderAttackEvent
import catmoe.fallencrystal.moefilter.network.limbo.handler.MoeLimbo
import catmoe.fallencrystal.moefilter.util.message.notification.Notifications
import catmoe.fallencrystal.moefilter.util.message.v2.MessageUtil
import catmoe.fallencrystal.moefilter.util.plugin.util.Scheduler
import catmoe.fallencrystal.translation.event.EventListener
import catmoe.fallencrystal.translation.event.annotations.AsynchronousHandler
import catmoe.fallencrystal.translation.event.annotations.EventHandler
import catmoe.fallencrystal.translation.event.annotations.HandlerPriority
import catmoe.fallencrystal.translation.event.events.player.PlayerJoinEvent
import catmoe.fallencrystal.translation.platform.Platform
import catmoe.fallencrystal.translation.platform.ProxyPlatform
import catmoe.fallencrystal.translation.player.bungee.BungeePlayer
import catmoe.fallencrystal.translation.utils.config.LocalConfig
import catmoe.fallencrystal.translation.utils.version.Version
import catmoe.fallencrystal.translation.utils.webhook.WebhookSender
import com.typesafe.config.Config
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.connection.ProxiedPlayer

class AttackCounterListener : EventListener {
    private var inAttack = false
    private val scheduler = Scheduler.getDefault()

    @EventHandler(UnderAttackEvent::class, priority = HandlerPriority.LOWEST)
    @AsynchronousHandler
    @Suppress("UNUSED_PARAMETER")
    @Platform(ProxyPlatform.BUNGEE)
    fun startSessionCount(event: UnderAttackEvent) {
        if (!inAttack) {
            inAttack=true; ConnectionStatistics.inAttack=true
            ProxyServer.getInstance().players.forEach {
                if (it.hasPermission("moefilter.notification.auto.actionbar") || it.hasPermission("moefilter.notification.auto")) Notifications.autoNotification.add(it)
            }
            MessageUtil.logWarn("[MoeFilter] [AntiBot] The server is under attack!")
            sendTitle(true)
            WebhookSender().sendWebhook(LocalConfig.getAntibot().getConfig("notifications.webhook.attack-start"))
        }
    }

    @Suppress("UNUSED_PARAMETER")
    @EventHandler(AttackStoppedEvent::class, priority = HandlerPriority.LOWEST)
    @AsynchronousHandler
    @Platform(ProxyPlatform.BUNGEE)
    fun stopSessionCount(event: AttackStoppedEvent) {
        if (inAttack) {
            inAttack=false; ConnectionStatistics.inAttack=false
            MoeLimbo.calibrateConnections()
            MessageUtil.logWarn("[MoeFilter] [AntiBot] The attack seems is stopped")
            ConnectionStatistics.sessionIpCache.invalidateAll()
            Notifications.autoNotification.clear()
            sendTitle(false)
            WebhookSender().sendWebhook(LocalConfig.getAntibot().getConfig("notifications.webhook.attack-stopped"))
        }
    }

    @EventHandler(PlayerJoinEvent::class, priority = HandlerPriority.LOWEST)
    @Platform(ProxyPlatform.BUNGEE)
    fun onPlayerJoin(event: PlayerJoinEvent) {
        if (StateManager.inAttack.get() && event.player.hasPermission("moefilter.notifications.auto"))
            Notifications.autoNotification.add((event.player.upstream as BungeePlayer).player)
    }

    private fun sendTitle(inAttack: Boolean /* true = start; false = stopped */) {
        val p = LocalConfig.getAntibot().getConfig("notifications.title")
        val config = when (inAttack) {
            true -> p.getConfig("attack-start")
            false -> p.getConfig("attack-stopped")
        }
        if (config.getBoolean("enabled")) {
            for (i in ProxyServer.getInstance().players) {
                if (i.hasPermission("moefilter.notification.auto.title") || i.hasPermission("moefilter.notification.auto")) { sendTitle(config, i) }
            }
        }
    }

    private fun sendTitle(config: Config, player: ProxiedPlayer) {
        val hex = player.pendingConnection.version >= Version.V1_16.number
        val title = MessageUtil.colorize(config.getString("title"), hex)
        val subtitle = MessageUtil.colorize(config.getString("sub-title"), hex)
        val obj = ProxyServer.getInstance().createTitle()
        obj.title(title)
        obj.subTitle(subtitle)
        obj.fadeIn(config.getInt("fade-in"))
        obj.stay(config.getInt("stay"))
        obj.fadeOut(config.getInt("fade-out"))
        player.sendTitle(obj)
    }
}