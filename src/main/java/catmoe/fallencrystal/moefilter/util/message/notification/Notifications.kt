package catmoe.fallencrystal.moefilter.util.message.notification

import catmoe.fallencrystal.moefilter.common.config.ObjectConfig
import catmoe.fallencrystal.moefilter.common.utils.counter.ConnectionCounter
import catmoe.fallencrystal.moefilter.common.utils.system.CPUMonitor
import catmoe.fallencrystal.moefilter.util.message.MessageUtil
import catmoe.fallencrystal.moefilter.util.plugin.FilterPlugin
import catmoe.fallencrystal.moefilter.util.plugin.util.Scheduler
import com.github.benmanes.caffeine.cache.Caffeine
import net.md_5.bungee.UserConnection
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.scheduler.ScheduledTask
import net.md_5.bungee.chat.ComponentSerializer
import net.md_5.bungee.protocol.ProtocolConstants
import net.md_5.bungee.protocol.packet.SystemChat
import net.md_5.bungee.protocol.packet.Title
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

    private val messagePacketCache = Caffeine.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES).build<String, ChatViaActionbarPackets>()
    private val componentSerializerCache = Caffeine.newBuilder().build<String, String>()

    private var schedule: ScheduledTask? = null

    private fun initSchedule() {
        this.schedule=scheduler.repeatScheduler(ObjectConfig.getMessage().getInt("actionbar.update-delay") * 50.toLong(), TimeUnit.MILLISECONDS) { onBroadcast() }
    }

    private fun onBroadcast() {
        val config = ObjectConfig.getMessage()
        val internalPlaceholder = mapOf(
            "%process_cpu%" to CPUMonitor.getRoundedCpuUsage().processCPU.toString(),
            "%system_cpu%" to CPUMonitor.getRoundedCpuUsage().systemCPU.toString(),
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

    private fun sendActionbar(players: List<ProxiedPlayer>, string: String) {
        val viaPacket = getViaPacket(string)
        for (player in players) {
            val uc = player as UserConnection
            val version = player.pendingConnection.version
            if (version > ProtocolConstants.MINECRAFT_1_17) { uc.unsafe().sendPacket(viaPacket.v117) }
            else if (version > ProtocolConstants.MINECRAFT_1_10) { uc.unsafe().sendPacket(viaPacket.v111) }
            else { uc.unsafe().sendPacket(viaPacket.v110) }
        }
    }

    private val actionbar = ChatMessageType.ACTION_BAR.ordinal

    private fun getViaPacket(text: String): ChatViaActionbarPackets {
        val viaPacket = messagePacketCache.getIfPresent(text) ?: ChatViaActionbarPackets(
            getChatPacketVersion117(MessageUtil.colorizeMiniMessage(text)),
            getChatPacketVersion111(MessageUtil.colorizeMiniMessage(text)),
            getChatPacketVersion110(MessageUtil.colorizeMiniMessage(text)))
        messagePacketCache.getIfPresent(text) ?: messagePacketCache.put(text, viaPacket)
        return viaPacket
    }

    private fun getChatPacketVersion117(text: BaseComponent): SystemChat { return SystemChat(componentSerializer(text), actionbar) }

    private fun getChatPacketVersion110(text: BaseComponent): SystemChat { return SystemChat(componentSerializer(TextComponent(BaseComponent.toLegacyText(text))), actionbar) }

    private fun getChatPacketVersion111(text: BaseComponent): Title {
        val title = Title()
        title.action=Title.Action.ACTIONBAR
        title.text = componentSerializer(text)
        return title
    }

    private fun componentSerializer(target: BaseComponent): String {
        val cs = componentSerializerCache.getIfPresent(target.toString()) ?: ComponentSerializer.toString(target);
        componentSerializerCache.put(target.toString(), cs); return cs
    }
}