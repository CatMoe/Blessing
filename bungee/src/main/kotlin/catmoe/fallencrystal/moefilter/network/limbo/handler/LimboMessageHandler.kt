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

import catmoe.fallencrystal.moefilter.event.PluginReloadEvent
import catmoe.fallencrystal.moefilter.network.common.kick.DisconnectType
import catmoe.fallencrystal.moefilter.network.common.kick.FastDisconnect
import catmoe.fallencrystal.moefilter.network.limbo.compat.message.NbtMessage
import catmoe.fallencrystal.moefilter.network.limbo.util.LimboCheckPassedEvent
import catmoe.fallencrystal.moefilter.network.limbo.util.LimboMessage
import catmoe.fallencrystal.moefilter.network.limbo.util.LimboMessage.Title
import catmoe.fallencrystal.moefilter.network.limbo.util.LimboMessage.Title.TitleTime
import catmoe.fallencrystal.moefilter.util.plugin.util.Scheduler
import catmoe.fallencrystal.translation.event.EventListener
import catmoe.fallencrystal.translation.event.annotations.EventHandler
import catmoe.fallencrystal.translation.event.annotations.HandlerPriority
import catmoe.fallencrystal.translation.utils.component.ComponentUtil
import catmoe.fallencrystal.translation.utils.config.LocalConfig
import catmoe.fallencrystal.translation.utils.config.Reloadable
import com.github.benmanes.caffeine.cache.Caffeine
import com.typesafe.config.Config
import net.md_5.bungee.api.scheduler.ScheduledTask
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.schedule

object LimboMessageHandler : Reloadable, EventListener {

    private val holderCache = Caffeine.newBuilder().build<LimboHandler, MessageSendHolder>()

    private var config = LocalConfig.getLimbo().getConfig("chat")
    private var checkingMessage = MessageGroup.create(config.getConfig("checking"))
    private var checkedMessage = MessageGroup.create(config.getConfig("check-passed"))

    class MessageGroup private constructor(config: Config) {
        private val title = if (config.getBoolean("title.send")) Title(
            getNbtMessage(config.getString("title.title")),
            getNbtMessage(config.getString("title.subtitle")),
            TitleTime(
                config.getInt("title.fade-in"),
                config.getInt("title.stay"),
                config.getInt("title.fade-out")
            )
        ) else null
        private val chat = if (config.getBoolean("chat.send"))
            getNbtMessage(config.getString("chat.message")) else null
        private val actionBar = if (config.getBoolean("action-bar.send"))
            getNbtMessage(config.getString("action-bar.message")) else null

        fun send(handler: LimboHandler) {
            val util = LimboMessage(handler)
            title?.let { util.sendTitle(it) }
            chat?.let { util.sendMessage(it) }
            actionBar?.let { util.sendActionbar(it) }
        }

        companion object {
            fun create(config: Config) =
                if (config.getBoolean("send")) MessageGroup(config) else null
        }
    }

    class MessageSendHolder(
        val handler: LimboHandler,
        private var group: MessageGroup
    ) {
        var scheduler: ScheduledTask? = null

        fun init(): MessageSendHolder {
            holderCache.put(handler, this)
            scheduler = Scheduler.getDefault().repeatScheduler(
                config.getLong("checking.schedule"),
                TimeUnit.MILLISECONDS
            ) {
                if (handler.channel.isActive) {
                    group.send(handler)
                } else {
                    scheduler?.cancel()
                    holderCache.invalidate(handler)
                }
            }
            return this
        }

        fun handleVerified(cancelMessage: MessageGroup) {
            this.group=cancelMessage
            scheduler?.cancel()
            holderCache.invalidate(handler)
            cancelMessage.send(handler)
        }

        companion object {
            fun create(handler: LimboHandler) =
                if (holderCache.getIfPresent(handler) == null)
                    checkingMessage?.let { MessageSendHolder(handler, it).init() }
                else null
        }
    }

    @EventHandler(LimboCheckPassedEvent::class, HandlerPriority.HIGHEST)
    fun checkPassed(event: LimboCheckPassedEvent) {
        val handler = event.handler
        val checkedMessage = this.checkedMessage
        fun kick() = FastDisconnect.disconnect(handler, DisconnectType.PASSED_CHECK)
        val holder = holderCache.getIfPresent(handler)
        if (checkedMessage != null && holder != null && holder.scheduler != null) {
            holder.handleVerified(checkedMessage)
            Timer().schedule(config.getLong("check-passed.delay-kick")) { kick() }
        } else kick()
    }

    @EventHandler(PluginReloadEvent::class, HandlerPriority.LOW)
    fun reload(event: PluginReloadEvent) {
        if (event.executor != null) this.reload()
    }

    private fun getNbtMessage(message: String) = NbtMessage.create(ComponentUtil.parse(message))

    override fun reload() {
        config = LocalConfig.getLimbo().getConfig("chat")
        checkingMessage = MessageGroup.create(config.getConfig("checking"))
        checkedMessage = MessageGroup.create(config.getConfig("check-passed"))
    }
}