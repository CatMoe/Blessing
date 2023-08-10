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

import catmoe.fallencrystal.moefilter.api.event.EventListener
import catmoe.fallencrystal.moefilter.api.event.FilterEvent
import catmoe.fallencrystal.moefilter.api.event.events.AttackStoppedEvent
import catmoe.fallencrystal.moefilter.api.event.events.UnderAttackEvent
import catmoe.fallencrystal.moefilter.api.event.events.bungee.AsyncPostLoginEvent
import catmoe.fallencrystal.moefilter.common.config.LocalConfig
import catmoe.fallencrystal.moefilter.common.counter.ConnectionCounter
import catmoe.fallencrystal.moefilter.common.utils.webhook.WebhookSender
import catmoe.fallencrystal.moefilter.util.message.notification.Notifications
import catmoe.fallencrystal.moefilter.util.message.v2.MessageUtil

class AttackCounterListener : EventListener {
    private var inAttack = false
    @FilterEvent
    @Suppress("UNUSED_PARAMETER")
    fun startSessionCount(event: UnderAttackEvent) {
        if (!inAttack) {
            inAttack=true; ConnectionCounter.setInAttack(true)
            Notifications.autoNotificationPlayer()
            WebhookSender().sendWebhook(LocalConfig.getConfig().getConfig("notifications.webhook.attack-start"))
            MessageUtil.logWarn("[MoeFilter] [AntiBot] The server is under attack!")
        }
    }

    @FilterEvent
    @Suppress("UNUSED_PARAMETER")
    fun stopSessionCount(event: AttackStoppedEvent) {
        if (inAttack) {
            inAttack=false; ConnectionCounter.setInAttack(false)
            Notifications.autoNotification.clear()
            WebhookSender().sendWebhook(LocalConfig.getConfig().getConfig("notifications.webhook.attack-stopped"))
            MessageUtil.logWarn("[MoeFilter] [AntiBot] The attack seems is stopped")
            ConnectionCounter.sessionIpCache.invalidateAll()
        }
    }

    @FilterEvent
    fun onPlayerJoin(event: AsyncPostLoginEvent) {
        if (StateManager.inAttack.get() && event.player.hasPermission("moefilter.notifications.auto"))
            Notifications.autoNotification.add(event.player)
    }
}