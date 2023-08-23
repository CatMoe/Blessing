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

import catmoe.fallencrystal.moefilter.MoeFilterBungee
import catmoe.fallencrystal.translation.utils.config.LocalConfig
import catmoe.fallencrystal.moefilter.common.counter.ConnectionCounter
import catmoe.fallencrystal.moefilter.state.AttackState
import catmoe.fallencrystal.moefilter.common.state.StateManager
import catmoe.fallencrystal.translation.utils.system.CPUMonitor
import catmoe.fallencrystal.moefilter.network.bungee.util.bconnection.ConnectionUtil
import catmoe.fallencrystal.moefilter.network.limbo.handler.MoeLimbo
import catmoe.fallencrystal.moefilter.util.message.v2.MessageUtil
import catmoe.fallencrystal.moefilter.util.message.v2.packet.type.MessagesType
import catmoe.fallencrystal.moefilter.util.plugin.util.Scheduler
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.scheduler.ScheduledTask
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit

@Suppress("SameParameterValue")
object Notifications {
    /*
    Don't put val ObjectConfig.getMessage() here.
    It will cause the config to not modify after the class is initialized.
     */

    private val scheduler = Scheduler(MoeFilterBungee.instance)

    init { initSchedule() }

    val switchNotification: MutableList<ProxiedPlayer> = CopyOnWriteArrayList()
    val autoNotification: MutableList<ProxiedPlayer> = CopyOnWriteArrayList()

    private var schedule: ScheduledTask? = null

    private var latestMessage = ""
    private var conf = LocalConfig.getMessage().getConfig("statistics")
    private var attackMessage = conf.getString("actionbar-format.attack")
    private var idleMessage = conf.getString("actionbar-format.idle")
    private var delay: Long = conf.getLong("actionbar-update-delay")

    private fun initSchedule() {
        this.schedule=scheduler.repeatScheduler(
            delay * 50.toLong(),
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
            "%total%" to "${ConnectionCounter.total}",
            "%total_session%" to "${ConnectionCounter.sessionTotal}",
            "%total_ips%" to "${ConnectionCounter.totalIps}",
            "%total_ips_session%" to "${ConnectionCounter.sessionTotalIps}",
            "%blocked%" to "${ConnectionCounter.totalBlocked()}",
            "%blocked_session%" to "${ConnectionCounter.totalSessionBlocked()}",
            "%peak_cps%" to "${ConnectionCounter.peakCps}",
            "%peak_cps_session%" to "${ConnectionCounter.sessionPeakCps}",
            "%prefix%" to config.getString("prefix"),
            "%duration%" to getDuration(StateManager.duration.getDuration()),
            "%type%" to getType(),
            "%limbo%" to MoeLimbo.connections.size.toString(),
        )
        internalPlaceholder.forEach { output = output.replace(it.key, it.value) }
        return output
    }

    private fun onBroadcast() {
        if (autoNotification.isEmpty() && switchNotification.isEmpty()) return
        if (autoNotification.isNotEmpty()) { autoNotification.removeAll(switchNotification.toSet()) }
        val message = placeholder(if (StateManager.inAttack.get()) attackMessage else idleMessage)
        if (autoNotification.isNotEmpty()) { sendActionbar(autoNotification, message) }
        if (switchNotification.isNotEmpty()) { sendActionbar(switchNotification, message) }
    }
    fun autoNotificationPlayer() { ProxyServer.getInstance().players.forEach {
        if (it.hasPermission("moefilter.notifications.auto")) { autoNotification.add(it) } }
    }

    fun reload() {
        conf = LocalConfig.getMessage().getConfig("statistics")
        delay = conf.getLong("actionbar-update-delay")
        attackMessage = conf.getString("actionbar-format.attack")
        idleMessage = conf.getString("actionbar-format.idle")
        // reset schedule task
        if (schedule != null) { scheduler.cancelTask(schedule!!); initSchedule() }
        initType()
    }

    private fun getDuration(sec: Long): String {
        // Hours, Minutes, Seconds or Minutes, Seconds
        return if (sec >= 3600) String.format("%02d:%02d:%02d", sec / 3600, sec % 3600 / 60, sec % 60)
        else String.format("%02d:%02d", sec % 3600 / 60, sec % 60)
    }

    /* AttackType get */

    private val typeMap: MutableMap<AttackState, String> = EnumMap(AttackState::class.java)

    private fun initType() {
         try { AttackState.values().forEach { typeMap[it] = conf.getString("types.${it.raw.lowercase()}") } } catch (_: NullPointerException) {  }
    }

    private fun getType(): String {
        val methods = StateManager.attackMethods
        if (methods.isEmpty()) return conf.getString("types.null")
        val sb = StringBuilder()
        val appear = conf.getString("types.join-to-line")
        var count = 0
        methods.forEach {
            val method = typeMap[it]
            if (method == null) { initType(); return "" }
            if (count != methods.size - 1) {
                sb.append(typeMap[it]).append(appear)
                count++
            } else { sb.append(typeMap[it]) }

        }
        return sb.toString()
    }


    private fun sendActionbar(players: List<ProxiedPlayer>, string: String) {
        if (latestMessage != string) { MessageUtil.invalidateCache(MessagesType.ACTION_BAR, string); latestMessage = string }
        players.forEach {
            try { MessageUtil.sendMessage(string, MessagesType.ACTION_BAR , ConnectionUtil(it.pendingConnection)) }
            catch (_: NullPointerException) { autoNotification.remove(it); switchNotification.remove(it) }
            catch (_: NoSuchFieldException) { autoNotification.remove(it); switchNotification.remove(it) }
        }
    }
}