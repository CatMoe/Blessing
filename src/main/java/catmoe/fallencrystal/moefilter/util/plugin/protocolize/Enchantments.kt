package catmoe.fallencrystal.moefilter.util.plugin.protocolize

import java.util.*

@Suppress("SpellCheckingInspection", "unused")
enum class Enchantments(var level: Int) {
    MENDING(0),
    UNBREAKING(0),
    CURSE_OF_VANISHING(0),
    AQUA_AFFINITY(0),
    BLAST_PROTECTION(0),
    CURSE_OF_BINDING(0),
    DEPTH_STRIDER(0),
    FEATHER_FALLING(0),
    FIRE_PROTECTION(0),
    FROST_WALKER(0),
    PROJECTILE_PROTECTION(0),
    PROTECTION(0),
    RESPIRATION(0),
    SOUL_SPEED(0),
    THORNS(0),
    BANE_OF_ARTHROPODS(0),
    FIRE_ASPECT(0),
    LOOTING(0),
    IMPALING(0),
    KNOCKBACK(0),
    SHARPNESS(0),
    SMITE(0),
    SWEEPING_EDGE(0),
    CHANNELING(0),
    FLAME(0),
    INFINITY(0),
    LOYALTY(0),
    RIPTIDE(0),
    MULTISHOT(0),
    PIERCING(0),
    POWER(0),
    PUNCH(0),
    QUICK_CHARGE(0),
    EFFICIENCY(0),
    FORTUNE(0),
    LUCK_OF_THE_SEA(0),
    LURE(0),
    SILK_TOUCH(0);

    val string: String
        get() = "minecraft:" + this.toString().lowercase(Locale.getDefault())

    fun display(): String { return (this.toString().lowercase(Locale.getDefault()).replace("_", " ")).lowercase(Locale.getDefault()) }

    fun toString(enchantments: Enchantments, level: Int): String { return "d: \"" + enchantments.string + "\", lvl: " + level + "s" }
}