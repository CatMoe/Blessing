package catmoe.fallencrystal.moefilter.util.message.notification

import catmoe.fallencrystal.moefilter.common.config.ObjectConfig
import catmoe.fallencrystal.moefilter.common.utils.counter.ConnectionCounter
import catmoe.fallencrystal.moefilter.common.utils.system.CPUMonitor
import catmoe.fallencrystal.moefilter.util.message.MessageUtil
import catmoe.fallencrystal.moefilter.util.plugin.FilterPlugin
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.connection.ProxiedPlayer
import java.util.concurrent.TimeUnit

object Notifications {

    init { ProxyServer.getInstance().scheduler.schedule(FilterPlugin.getPlugin(),{onBroadcast()},0,100,TimeUnit.MILLISECONDS) }

    private val messagePrefix = ObjectConfig.getMessage().getString("prefix")
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
        )
        if (autoNotificationPlayer.isNotEmpty()) { autoNotificationPlayer.removeAll(spyNotificationPlayers) }
        // Don't use val here. it will lock message style.
        val message = ("$messagePrefix &bCPU &7proc. &f%process_cpu%% &7sys. &f%system_cpu%% &8- &bCPS &f%cps% &8- &bIpSec &f%ipsec% &8- &bTotal &f%total%")
        var output = message
        internalPlaceholder.forEach { (placeholder, value) -> output = output.replace(placeholder, value) }
        if (autoNotificationPlayer.isNotEmpty()) { MessageUtil.sendMessage(autoNotificationPlayer, ChatMessageType.ACTION_BAR, output) }
        if (spyNotificationPlayers.isNotEmpty()) { MessageUtil.sendMessage(spyNotificationPlayers, ChatMessageType.ACTION_BAR, output) }
    }
    fun onAddAutoNotificationPlayer() { ProxyServer.getInstance().players.forEach { if (it.hasPermission("moefilter.notifications.auto")) { autoNotificationPlayer.add(it) } } }
    fun onInvalidateAutoNotificationPlayer() { autoNotificationPlayer.clear() }
    fun getAutoNotificationPlayer(): List<ProxiedPlayer> { return autoNotificationPlayer }

    fun toggleSpyNotificationPlayer(player: ProxiedPlayer): Boolean { return if (spyNotificationPlayers.contains(player)) { spyNotificationPlayers.remove(player); false } else { spyNotificationPlayers.add(player); true } }
    fun getSpyNotificationPlayers(): List<ProxiedPlayer> { return spyNotificationPlayers }
}