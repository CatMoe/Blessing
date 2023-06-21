package catmoe.fallencrystal.moefilter.util.message.notification

import catmoe.fallencrystal.moefilter.common.config.ObjectConfig
import catmoe.fallencrystal.moefilter.common.utils.counter.ConnectionCounter
import catmoe.fallencrystal.moefilter.common.utils.system.CPUMonitor
import catmoe.fallencrystal.moefilter.util.message.MessageUtil
import catmoe.fallencrystal.moefilter.util.plugin.FilterPlugin
import catmoe.fallencrystal.moefilter.util.plugin.util.Scheduler
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.scheduler.ScheduledTask
import java.util.concurrent.TimeUnit

object Notifications {
    /*
    Don't put val ObjectConfig.getMessage() here.
    It will cause the config to not modify after the class is initialized.
     */

    private val scheduler = Scheduler(FilterPlugin.getPlugin()!!)

    init { initSchedule() }

    private val spyNotificationPlayers: MutableList<ProxiedPlayer> = ArrayList()
    private val autoNotificationPlayer: MutableList<ProxiedPlayer> = ArrayList()

    private var schedule: ScheduledTask? = null

    private fun initSchedule() {
        this.schedule=scheduler.repeatScheduler(ObjectConfig.getMessage().getInt("actionbar.update-delay") * 50.toLong(), TimeUnit.MILLISECONDS) { onBroadcast() }
    }

    private fun onBroadcast() {
        val config = ObjectConfig.getMessage()
        val internalPlaceholder = mapOf(
            "%process_cpu%" to CPUMonitor.getRoundedCPUUsage().processCPU.toString(),
            "%system_cpu%" to CPUMonitor.getRoundedCPUUsage().systemCPU.toString(),
            "%cps%" to ConnectionCounter.getConnectionPerSec().toString(),
            "%ipsec%" to ConnectionCounter.getIpPerSec().toString(),
            "%total%" to ConnectionCounter.getTotal().toString(),
            "%total_session%" to ConnectionCounter.getTotalSession().toString(),
            "%peak_cps%" to ConnectionCounter.getPeakConnectionPerSec().toString(),
            "%prefix%" to config.getString("prefix"),
        )
        if (autoNotificationPlayer.isNotEmpty()) { autoNotificationPlayer.removeAll(spyNotificationPlayers) }
        val message = config.getString("actionbar.style")
        var output = message
        internalPlaceholder.forEach { (placeholder, value) -> output = output.replace(placeholder, value) }
        if (autoNotificationPlayer.isNotEmpty()) { sendActionbar(autoNotificationPlayer, output) }
        if (spyNotificationPlayers.isNotEmpty()) { sendActionbar(spyNotificationPlayers, output) }
    }
    fun onAddAutoNotificationPlayer() { ProxyServer.getInstance().players.forEach { if (it.hasPermission("moefilter.notifications.auto")) { autoNotificationPlayer.add(it) } } }

    fun onInvalidateAutoNotificationPlayer() { autoNotificationPlayer.clear() }

    fun toggleSpyNotificationPlayer(player: ProxiedPlayer): Boolean { return if (spyNotificationPlayers.contains(player)) { spyNotificationPlayers.remove(player); false } else { spyNotificationPlayers.add(player); true } }

    fun reload() {
        // reset schedule task
        if (schedule != null) { scheduler.cancelTask(schedule!!); initSchedule() }
    }

    private fun sendActionbar(players: List<ProxiedPlayer>, string: String) { MessageUtil.sendActionbar(players, MessageUtil.colorizeMiniMessage(string)) }
}