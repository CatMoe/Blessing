package catmoe.fallencrystal.moefilter.util.message.notification

import catmoe.fallencrystal.moefilter.common.config.ObjectConfig
import catmoe.fallencrystal.moefilter.common.utils.counter.ConnectionCounter
import catmoe.fallencrystal.moefilter.common.utils.system.CPUMonitor
import catmoe.fallencrystal.moefilter.util.message.MessageUtil
import catmoe.fallencrystal.moefilter.util.plugin.FilterPlugin
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.connection.ProxiedPlayer
import java.util.concurrent.TimeUnit

object Notifications {

    init { ProxyServer.getInstance().scheduler.schedule(FilterPlugin.getPlugin(),{onBroadcast()},0,100,TimeUnit.MILLISECONDS) }

    private val spyNotificationPlayers: MutableList<ProxiedPlayer> = ArrayList()
    private val autoNotificationPlayer: MutableList<ProxiedPlayer> = ArrayList()

    private fun onBroadcast() {
        val internalPlaceholder = mapOf(
            "%process_cpu%" to CPUMonitor.getRoundedCPUUsage().processCPU.toString(),
            "%system_cpu%" to CPUMonitor.getRoundedCPUUsage().systemCPU.toString(),
            "%cps%" to ConnectionCounter.getConnectionPerSec().toString(),
            "%ipsec%" to ConnectionCounter.getIpPerSec().toString(),
            "%total%" to ConnectionCounter.getTotal().toString(),
            "%total_session%" to ConnectionCounter.getTotalSession().toString(),
            "%peak_cps%" to ConnectionCounter.getPeakConnectionPerSec().toString(),
            "%prefix%" to ObjectConfig.getMessage().getString("prefix"),
        )
        if (autoNotificationPlayer.isNotEmpty()) { autoNotificationPlayer.removeAll(spyNotificationPlayers) }
        /*
        Deprecated Messages:
        <aqua>CPU <gray>proc. <white>%process_cpu%% <gray>sys. <white>%system_cpu%% <dark_gray>- <aqua>CPS <white>%cps% <dark_gray>- <aqua>Peak <white>%peak_cps% <dark_gray>- <aqua>IpSec <white>%ipsec% <dark_gray>- <aqua>Total <white>%total%
        <gradient:green:yellow:aqua> CPU proc. %process_cpu%% sys, %system_cpu%% - CPS %cps% - Peak %peak_cps% - IpSec %ipsec% - Total %total%</gradient>
         */
        val message = ObjectConfig.getMessage().getString("actionbar.style")
        var output = message
        internalPlaceholder.forEach { (placeholder, value) -> output = output.replace(placeholder, value) }
        if (autoNotificationPlayer.isNotEmpty()) { sendActionbar(autoNotificationPlayer, output) }
        if (spyNotificationPlayers.isNotEmpty()) { sendActionbar(spyNotificationPlayers, output) }
    }
    fun onAddAutoNotificationPlayer() { ProxyServer.getInstance().players.forEach { if (it.hasPermission("moefilter.notifications.auto")) { autoNotificationPlayer.add(it) } } }
    fun onInvalidateAutoNotificationPlayer() { autoNotificationPlayer.clear() }
    fun getAutoNotificationPlayer(): List<ProxiedPlayer> { return autoNotificationPlayer }

    fun toggleSpyNotificationPlayer(player: ProxiedPlayer): Boolean { return if (spyNotificationPlayers.contains(player)) { spyNotificationPlayers.remove(player); false } else { spyNotificationPlayers.add(player); true } }
    fun getSpyNotificationPlayers(): List<ProxiedPlayer> { return spyNotificationPlayers }

    private fun sendActionbar(players: List<ProxiedPlayer>, string: String) { MessageUtil.sendActionbar(players, MessageUtil.colorizeMiniMessage(string)) }
}