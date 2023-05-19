package catmoe.fallencrystal.moefilter.util.plugin.protocolize.menu

/*
  Copy and edited from AkaneField - Author: FallenCrystal
  https://github.com/CatMoe/AkaneField/blob/main/src/main/java/catmoe/fallencrystal/akanefield/expansion/menu/utils/ForceFormatCode.kt
 */

object ForceFormatCode {
    @JvmStatic
    fun replaceFormat(str: String): String {
        return str
            .replace("&a", "§a")
            .replace("&b", "§b")
            .replace("&c", "§c")
            .replace("&d", "§d")
            .replace("&e", "§e")
            .replace("&f", "§f")
            .replace("&1", "§1")
            .replace("&2", "§2")
            .replace("&3", "§3")
            .replace("&4", "§4")
            .replace("&5", "§5")
            .replace("&6", "§6")
            .replace("&7", "§7")
            .replace("&8", "§8")
            .replace("&9", "§9")
            .replace("&k", "§k")
            .replace("&l", "§l")
            .replace("&m", "§m")
            .replace("&m", "§m")
            .replace("&o", "§o")
            .replace("&r", "§r")
    }
}