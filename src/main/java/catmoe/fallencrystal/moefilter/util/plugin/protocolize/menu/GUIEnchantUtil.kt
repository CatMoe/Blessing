package catmoe.fallencrystal.moefilter.util.plugin.protocolize.menu

/*
  Copy and edited from AkaneField - Author: FallenCrystal
https://github.com/CatMoe/AkaneField/blob/main/src/main/java/catmoe/fallencrystal/akanefield/expansion/menu/utils/GUIEnchantUtil.kt
 */

class GUIEnchantUtil @JvmOverloads constructor(@JvmField val enchantment: GUIEnchantsList, @JvmField val level: Int = 1) {
    override fun toString(): String { return "d: \"" + enchantment.string + "\", lvl: " + level + "s" }
}