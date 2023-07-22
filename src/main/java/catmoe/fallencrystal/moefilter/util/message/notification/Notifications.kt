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

package catmoe.fallencrystal.moefilter.util.message.notification

import catmoe.fallencrystal.moefilter.MoeFilter
import catmoe.fallencrystal.moefilter.common.config.LocalConfig
import catmoe.fallencrystal.moefilter.common.counter.ConnectionCounter
import catmoe.fallencrystal.moefilter.common.state.StateManager
import catmoe.fallencrystal.moefilter.common.utils.system.CPUMonitor
import catmoe.fallencrystal.moefilter.network.bungee.util.bconnection.ConnectionUtil
import catmoe.fallencrystal.moefilter.util.message.v2.MessageUtil
import catmoe.fallencrystal.moefilter.util.message.v2.packet.type.MessagesType
import catmoe.fallencrystal.moefilter.util.plugin.util.Scheduler
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.scheduler.ScheduledTask
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit

@Suppress("SameParameterValue")
object Notifications {
    /*
    Don't put val ObjectConfig.getMessage() here.
    It will cause the config to not modify after the class is initialized.
     */

    private val scheduler = Scheduler(MoeFilter.instance)

    init { initSchedule() }

    private val spyNotificationPlayers: MutableList<ProxiedPlayer> = CopyOnWriteArrayList()
    private val autoNotificationPlayer: MutableList<ProxiedPlayer> = CopyOnWriteArrayList()

    private var schedule: ScheduledTask? = null

    private var latestMessage = ""

    private fun initSchedule() {
        this.schedule=scheduler.repeatScheduler(
            LocalConfig.getMessage().getInt("actionbar.update-delay") * 50.toLong(),
            TimeUnit.MILLISECONDS)
        { onBroadcast() }
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun placeholder(message: String): String {
        var output = message
        val config = LocalConfig.getMessage()
        val cpu = CPUMonitor.getRoundedCpuUsage()
        val internalPlaceholder = mapOf(
            "%process_cpu%" to "${cpu.processCPU}",
            "%system_cpu%" to "${cpu.systemCPU}",
            "%cps%" to "${ConnectionCounter.getConnectionPerSec()}",
            "%ipsec%" to "${ConnectionCounter.getIpPerSec()}",
            "%total%" to "${ConnectionCounter.total}",
            "%total_session%" to "${ConnectionCounter.totalInSession}",
            "%blocked%" to "${ConnectionCounter.totalBlocked()}",
            "%blocked_session%" to "${ConnectionCounter.totalSessionBlocked()}",
            "%peak_cps%" to "${ConnectionCounter.peakCps}",
            "%peak_cps_session%" to "${ConnectionCounter.peakCpsInSession}",
            "%prefix%" to config.getString("prefix"),
            "%duration%" to getDuration(StateManager.duration.getDuration()),
            "%blocked%" to "${ConnectionCounter.totalBlocked()}",
        )
        internalPlaceholder.forEach { output = output.replace(it.key, it.value) }
        return output
    }

    @Suppress("MemberVisibilityCanBePrivate")
    private fun onBroadcast() {
        val config = LocalConfig.getMessage()
        if (autoNotificationPlayer.isNotEmpty()) { autoNotificationPlayer.removeAll(spyNotificationPlayers) }
        val message = placeholder(if (StateManager.inAttack.get()) { config.getString("actionbar.format.attack") } else config.getString("actionbar.format.idle"))
        if (autoNotificationPlayer.isNotEmpty()) { sendActionbar(autoNotificationPlayer, message) }
        if (spyNotificationPlayers.isNotEmpty()) { sendActionbar(spyNotificationPlayers, message) }
    }
    fun onAddAutoNotificationPlayer() { ProxyServer.getInstance().players.forEach { if (it.hasPermission("moefilter.notifications.auto")) { autoNotificationPlayer.add(it) } } }

    fun onInvalidateAutoNotificationPlayer() { autoNotificationPlayer.clear() }

    fun toggleSpyNotificationPlayer(player: ProxiedPlayer): Boolean { return if (spyNotificationPlayers.contains(player)) { spyNotificationPlayers.remove(player); false } else { spyNotificationPlayers.add(player); true } }

    fun reload() {
        // reset schedule task
        if (schedule != null) { scheduler.cancelTask(schedule!!); initSchedule() }
    }

    private fun getDuration(sec: Long): String {
        // Hours, Minutes, Seconds or Minutes, Seconds
        return if (sec >= 3600) String.format("%02d:%02d:%02d", sec / 3600, sec % 3600 / 60, sec % 60)
        else String.format("%02d:%02d", sec % 3600 / 60, sec % 60)
    }

    private fun sendActionbar(players: List<ProxiedPlayer>, string: String) {
        if (latestMessage != string) { MessageUtil.invalidateCache(MessagesType.ACTION_BAR, string); latestMessage = string }
        players.forEach {
            try { MessageUtil.sendMessage(string, MessagesType.ACTION_BAR , ConnectionUtil(it.pendingConnection)) }
            catch (_: NullPointerException) { autoNotificationPlayer.remove(it); spyNotificationPlayers.remove(it) }
        }
    }
}