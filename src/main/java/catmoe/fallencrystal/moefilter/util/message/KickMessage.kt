package catmoe.fallencrystal.moefilter.util.message

import catmoe.fallencrystal.moefilter.common.config.ObjectConfig

object KickMessage {
    private val placeholderConfig = ObjectConfig.getMessage().getConfig("kick.placeholders")
    private val placeholders = placeholderConfig.entrySet().associate { entry -> entry.key to entry.value.unwrapped().toString() }

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
}