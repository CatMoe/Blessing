package catmoe.fallencrystal.moefilter.util.message.kick

import catmoe.fallencrystal.moefilter.common.config.ObjectConfig
import com.github.benmanes.caffeine.cache.Caffeine

object KickMessage {
    private val placeholderConfig = ObjectConfig.getMessage().getConfig("kick.placeholders")
    private val placeholders = placeholderConfig.entrySet().associate { entry -> entry.key to entry.value.unwrapped().toString() }

    private val messageCache = Caffeine.newBuilder().build<KickType, List<String>>()

    fun replacePlaceholders(text: String): String {
        var content = text
        placeholders.forEach { (placeholder, value) ->
            val placeholderPattern = ObjectConfig.getMessage().getString("kick.placeholder-pattern").replace("[placeholder]", placeholder)
            content = content.replace(placeholderPattern, value)
        }
        return content
    }

    fun replacePlaceholders(reason: List<String>): List<String> {
        val str = mutableListOf<String>()
        for (item in reason) {
            var text = item
            for ((placeholder, value) in placeholders) {
                val placeholderPattern = ObjectConfig.getMessage().getString("kick.placeholder-pattern").replace("[placeholder]", placeholder)
                text = item.replace(placeholderPattern, value)
            }
            str.add(text)
        }
        return str
    }

    fun getKickedMessage(type: KickType): List<String> {
        val cache = messageCache.getIfPresent(type)
        if (cache != null) { return cache }
        val originalMessage = type.originalMessage
        val message = replacePlaceholders(originalMessage)
        messageCache.put(type, message)
        return message
    }
}