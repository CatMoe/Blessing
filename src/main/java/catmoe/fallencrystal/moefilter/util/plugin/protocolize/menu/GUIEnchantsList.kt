package catmoe.fallencrystal.moefilter.util.plugin.protocolize.menu

import java.util.*

/*
  Copy and edited from AkaneField - Author: FallenCrystal
  https://github.com/CatMoe/AkaneField/blob/main/src/main/java/catmoe/fallencrystal/akanefield/expansion/menu/utils/GUIEnchantsList.kt
 */

enum class GUIEnchantsList {
    MENDING,
    UNBREAKING,
    CURSE_OF_VANISHING,
    AQUA_AFFINITY,
    BLAST_PROTECTION,
    CURSE_OF_BINDING,
    DEPTH_STRIDER,
    FEATHER_FALLING,
    FIRE_PROTECTION,
    FROST_WALKER,
    PROJECTILE_PROTECTION,
    PROTECTION,
    RESPIRATION,
    SOUL_SPEED,
    THORNS,
    BANE_OF_ARTHROPODS,
    FIRE_ASPECT,
    LOOTING,
    IMPALING,
    KNOCKBACK,
    SHARPNESS,
    SMITE,
    SWEEPING_EDGE,
    CHANNELING,
    FLAME,
    INFINITY,
    LOYALTY,
    RIPTIDE,
    MULTISHOT,
    PIERCING,
    POWER,
    PUNCH,
    QUICK_CHARGE,
    EFFICIENCY,
    FORTUNE,
    LUCK_OF_THE_SEA,
    LURE,
    SILK_TOUCH;

    val string: String
        get() = "minecraft:" + this.toString().lowercase(Locale.getDefault())

    fun display(): String {
        val s = this.toString().lowercase(Locale.getDefault()).replace("_", " ")
        return s.lowercase(Locale.getDefault())
    }
}