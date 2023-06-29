package catmoe.fallencrystal.moefilter.util.message.notification

import catmoe.fallencrystal.moefilter.MoeFilter
import catmoe.fallencrystal.moefilter.common.config.LocalConfig
import catmoe.fallencrystal.moefilter.common.utils.counter.ConnectionCounter
import catmoe.fallencrystal.moefilter.common.utils.system.CPUMonitor
import catmoe.fallencrystal.moefilter.network.bungee.util.bconnection.ConnectionUtil
import catmoe.fallencrystal.moefilter.util.message.component.ComponentUtil
import catmoe.fallencrystal.moefilter.util.plugin.util.Scheduler
import com.github.benmanes.caffeine.cache.Caffeine
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.scheduler.ScheduledTask
import net.md_5.bungee.chat.ComponentSerializer
import net.md_5.bungee.protocol.ProtocolConstants
import net.md_5.bungee.protocol.packet.Chat
import net.md_5.bungee.protocol.packet.SystemChat
import net.md_5.bungee.protocol.packet.Title
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit

object Notifications {
    /*
    Don't put val ObjectConfig.getMessage() here.
    It will cause the config to not modify after the class is initialized.
     */

    private val scheduler = Scheduler(MoeFilter.instance)

    init { initSchedule() }

    private val spyNotificationPlayers: MutableList<ProxiedPlayer> = CopyOnWriteArrayList()
    private val autoNotificationPlayer: MutableList<ProxiedPlayer> = CopyOnWriteArrayList()

    private val messagePacketCache = Caffeine.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES).build<String, ChatViaActionbarPackets>()

    private var schedule: ScheduledTask? = null

    private var need119data = false
    private var need117data = false
    private var need111data = false
    private var need110data = false

    private fun initSchedule() {
        this.schedule=scheduler.repeatScheduler(LocalConfig.getMessage().getInt("actionbar.update-delay") * 50.toLong(), TimeUnit.MILLISECONDS) { onBroadcast() }
    }

    private fun onBroadcast() {
        val config = LocalConfig.getMessage()
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
        need117data=false; need111data=false; need110data=false
        for (player in players) {
            try { send(player, viaPacket) } catch (npe: NullPointerException) {
                autoNotificationPlayer.removeAll(listOf(player))
                spyNotificationPlayers.removeAll(listOf(player))
            }
        }
    }

    private fun send(player: ProxiedPlayer, packets: ChatViaActionbarPackets) {
        val connection = ConnectionUtil(player.pendingConnection)
        val version = connection.getVersion()
        try {
            if (version >= ProtocolConstants.MINECRAFT_1_19) { if (packets.has119data) { connection.writePacket(packets.v119!!) } else { need119data=true } }
            else if (version > ProtocolConstants.MINECRAFT_1_17) { if (packets.has117data) { connection.writePacket(packets.v117!!) } else { need117data=true } }
            else if (version > ProtocolConstants.MINECRAFT_1_10) { if (packets.has111data) { connection.writePacket(packets.v111!!) } else { need111data=true } }
            else { if (packets.has110data) { connection.writePacket(packets.v110!!) } else { need110data=true } }
        } catch (exception: Exception) { exception.printStackTrace() }
    }

    private val actionbar = ChatMessageType.ACTION_BAR.ordinal

    private fun getViaPacket(text: String): ChatViaActionbarPackets {
        // 在经过了一堆改善之后 这是最后修改的版本 我想我不会再要求改什么了
        val bc = ComponentUtil.toBaseComponents(ComponentUtil.parse(text))
        val componentSerializer = ComponentSerializer.toString(bc)
        val cached = messagePacketCache.getIfPresent(text)
        val v119 = if (cached?.has119data == true && need119data) cached.v119 else if (need119data) getChatPacketVersion119(componentSerializer) else null
        val v117 = if (cached?.has117data == true && need117data) cached.v117 else if (need117data) getChatPacketVersion117(componentSerializer) else null
        val v111 = if (cached?.has111data == true && need111data) cached.v111 else if (need111data) getChatPacketVersion111(componentSerializer) else null
        val v110 = if (cached?.has110data == true && need110data) cached.v110 else if (need110data) getChatPacketVersion110(bc) else null
        val viaPacket = ChatViaActionbarPackets(v119, v117, v111, v110, need119data, need117data, need111data, need110data)
        messagePacketCache.put(text, viaPacket)
        return viaPacket
    }

    private fun getChatPacketVersion119(text: String): SystemChat { return SystemChat(text, actionbar) }

    private fun getChatPacketVersion117(text: String): Chat { return Chat(text, actionbar.toByte(), null) }

    private fun getChatPacketVersion111(text: String): Title {
        val title = Title()
        title.action = Title.Action.ACTIONBAR
        title.text = text
        return title
    }

    private fun getChatPacketVersion110(text: BaseComponent): Chat { return Chat(ComponentSerializer.toString(TextComponent(BaseComponent.toLegacyText(text))), actionbar.toByte(), null) }

}