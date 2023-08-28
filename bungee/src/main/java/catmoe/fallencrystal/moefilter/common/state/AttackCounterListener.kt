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

import catmoe.fallencrystal.moefilter.MoeFilterBungee
import catmoe.fallencrystal.moefilter.api.event.EventListener
import catmoe.fallencrystal.moefilter.api.event.FilterEvent
import catmoe.fallencrystal.moefilter.api.event.events.AttackStoppedEvent
import catmoe.fallencrystal.moefilter.api.event.events.UnderAttackEvent
import catmoe.fallencrystal.moefilter.api.event.events.bungee.AsyncPostLoginEvent
import catmoe.fallencrystal.moefilter.common.counter.ConnectionCounter
import catmoe.fallencrystal.moefilter.common.utils.webhook.WebhookSender
import catmoe.fallencrystal.moefilter.network.limbo.handler.MoeLimbo
import catmoe.fallencrystal.moefilter.util.message.notification.Notifications
import catmoe.fallencrystal.moefilter.util.message.v2.MessageUtil
import catmoe.fallencrystal.moefilter.util.plugin.util.Scheduler
import catmoe.fallencrystal.translation.utils.config.LocalConfig
import catmoe.fallencrystal.translation.utils.version.Version
import com.typesafe.config.Config
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.connection.ProxiedPlayer

class AttackCounterListener : EventListener {
    private var inAttack = false
    private val scheduler = Scheduler(MoeFilterBungee.instance)
    @FilterEvent
    @Suppress("UNUSED_PARAMETER")
    fun startSessionCount(event: UnderAttackEvent) {
        if (!inAttack) {
            inAttack=true; ConnectionCounter.setInAttack(true)
            Notifications.autoNotificationPlayer()
            MessageUtil.logWarn("[MoeFilter] [AntiBot] The server is under attack!")
            scheduler.runAsync {
                sendTitle(true)
                WebhookSender().sendWebhook(LocalConfig.getAntibot().getConfig("notifications.webhook.attack-start"))
            }
        }
    }

    @FilterEvent
    @Suppress("UNUSED_PARAMETER")
    fun stopSessionCount(event: AttackStoppedEvent) {
        if (inAttack) {
            inAttack=false; ConnectionCounter.setInAttack(false)
            MoeLimbo.calibrateConnections()
            MessageUtil.logWarn("[MoeFilter] [AntiBot] The attack seems is stopped")
            ConnectionCounter.sessionIpCache.invalidateAll()
            scheduler.runAsync {
                Notifications.autoNotification.clear()
                sendTitle(false)
                WebhookSender().sendWebhook(LocalConfig.getAntibot().getConfig("notifications.webhook.attack-stopped"))
            }
        }
    }

    @FilterEvent
    fun onPlayerJoin(event: AsyncPostLoginEvent) {
        if (StateManager.inAttack.get() && event.player.hasPermission("moefilter.notifications.auto"))
            Notifications.autoNotification.add(event.player)
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