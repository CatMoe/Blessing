package catmoe.fallencrystal.moefilter.network.bungee.util.kick

import catmoe.fallencrystal.moefilter.common.config.LocalConfig
import catmoe.fallencrystal.moefilter.network.bungee.util.bconnection.ConnectionUtil
import catmoe.fallencrystal.moefilter.util.message.v2.MessageUtil
import com.github.benmanes.caffeine.cache.Caffeine
import io.netty.channel.Channel
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.chat.ComponentSerializer
import net.md_5.bungee.protocol.packet.Kick

object FastDisconnect {
    private val reasonCache = Caffeine.newBuilder().build<DisconnectType, DisconnectReason>()

    private fun getPlaceholders(): Map<String, String> {
        val placeholderConfig = LocalConfig.getMessage().getConfig("kick.placeholders")
        val resultMap: MutableMap<String, String> = mutableMapOf()
        for (key in placeholderConfig.root().keys) { resultMap[LocalConfig.getMessage().getString("kick.placeholder-pattern").replace("[placeholder]", key)]=placeholderConfig.getString(key) }
        return resultMap
    }

    fun disconnect(channel: Channel, type: DisconnectType) {
        val packet = (reasonCache.getIfPresent(type) ?: getCacheReason(type, TextComponent(""))).packet
        channel.writeAndFlush(packet); channel.close()
    }

    fun disconnect(connection: ConnectionUtil, type: DisconnectType) {
        if (connection.isConnected()) {
            val packet = (reasonCache.getIfPresent(type) ?: getCacheReason(type, TextComponent(""))).packet
            connection.writePacket(packet); connection.close()
        }
    }

    fun initMessages() {
        val placeholder = getPlaceholders()
        for (type in DisconnectType.values()) {
            // <newline> is MiniMessage's syntax. use it instead of \n
            val message = MessageUtil.colorize(replacePlaceholder(LocalConfig.getMessage().getStringList(type.messagePath).joinToString("<reset><newline>"), placeholder))
            reasonCache.put(type, getCacheReason(type, message))
        }
    }

    private fun replacePlaceholder(message: String, placeholder: Map<String, String>): String { var output = message; placeholder.forEach { output=output.replace(it.key, it.value) }; return output }

    private fun getCacheReason(type: DisconnectType, baseComponent: BaseComponent): DisconnectReason { return DisconnectReason(type, baseComponent, Kick(ComponentSerializer.toString(baseComponent))) }
}