package catmoe.fallencrystal.moefilter.util.plugin.protocolize.menu

import dev.simplix.protocolize.api.item.BaseItemStack
import dev.simplix.protocolize.api.item.ItemStack
import dev.simplix.protocolize.data.ItemType
import net.querz.nbt.tag.CompoundTag
import net.querz.nbt.tag.IntTag
import net.querz.nbt.tag.ListTag
import java.util.*
import java.util.function.Consumer
import java.util.stream.Collectors

/*
  Copy and edited from AkaneField - Author: FallenCrystal
  https://github.com/CatMoe/AkaneField/blob/main/src/main/java/catmoe/fallencrystal/akanefield/expansion/menu/utils/ItemBuilder.kt
 */

class ItemBuilder(material: ItemType?) {
    private val item: ItemStack
    private val enchantments: MutableList<GUIEnchantUtil> = ArrayList()
    private var hideEnchants = false
    private var tags: MutableList<CompoundTag> = ArrayList()

    init {
        item = ItemStack(material)
    }

    fun type(material: ItemType?): ItemBuilder {
        item.itemType(material)
        return this
    }

    fun amount(amount: Int): ItemBuilder {
        item.amount(amount.toByte())
        return this
    }

    fun name(string: String?): ItemBuilder {
        item.displayName(replaceFormat(string!!))
        return this
    }

    fun lore(string: String?): ItemBuilder {
        item.addToLore(replaceFormat(string!!))
        return this
    }

    fun lore(lores: List<String?>): ItemBuilder {
        lores.forEach(Consumer { string: String? -> this.lore(string) })
        return this
    }

    fun lores(lores: Array<String>?): ItemBuilder {
        Arrays.stream(lores).collect(Collectors.toList()).forEach(
            Consumer { string: String? -> this.lore(string) })
        return this
    }

    fun durability(durability: Short): ItemBuilder {
        item.durability(durability)
        return this
    }

    fun enchantment(enchantment: GUIEnchantUtil): ItemBuilder {
        enchantments.add(enchantment)
        return this
    }

    fun enchantments(enchantments: Array<GUIEnchantUtil>): ItemBuilder {
        this.enchantments.clear()
        this.enchantments.addAll(listOf(*enchantments))
        return this
    }

    fun enchantments(enchantment: Array<GUIEnchantsList>?, level: Int): ItemBuilder {
        enchantments.clear()
        Arrays.stream(enchantment).forEach { e: GUIEnchantsList? -> enchantment(e, level) }
        return this
    }

    fun enchantment(enchantment: GUIEnchantsList?, level: Int): ItemBuilder {
        enchantments.add(GUIEnchantUtil(enchantment!!, level))
        return this
    }

    fun clearEnchantment(enchantments: GUIEnchantsList): ItemBuilder {
        val temp: List<GUIEnchantUtil> = ArrayList(this.enchantments)
        temp.forEach(Consumer { en: GUIEnchantUtil -> if (en.enchantment === enchantments) this.enchantments.remove(en) })
        return this
    }

    fun clearEnchantments(): ItemBuilder {
        enchantments.clear()
        return this
    }

    fun clearLore(c: String?): ItemBuilder {
        // TextComponent
        item.lore<Any>().remove(replaceFormat(c!!))
        return this
    }

    fun clearLores(): ItemBuilder {
        item.lore(ArrayList<Any?>(), false)
        return this
    }

    fun clearLores(i: Int): ItemBuilder {
        val newList = item.lore<String?>()
        newList.subList(i, newList.size).clear()
        item.lore(newList, true)
        return this
    }

    fun clearLores(i1: Int, i2: Int): ItemBuilder {
        val newList = item.lore<String?>()
        newList.subList(i1, i2).clear()
        item.lore(newList, true)
        return this
    }

    fun skullOwner(name: String?): ItemBuilder {
        return this
    }

    fun hideEnchantments(hide: Boolean): ItemBuilder {
        hideEnchants = hide
        return this
    }

    fun addTag(tag: CompoundTag): ItemBuilder {
        tags.add(tag)
        return this
    }

    fun setTags(tags: MutableList<CompoundTag>): ItemBuilder {
        this.tags = tags
        return this
    }

    fun clearTags(): ItemBuilder {
        tags.clear()
        return this
    }

    fun getEnchantments(): List<GUIEnchantUtil> {
        return enchantments
    }

    fun build(): ItemStack {
        val nbt = item.nbtData()
        if (enchantments.isNotEmpty()) {
            val enchantments = ListTag.createUnchecked(
                CompoundTag::class.java
            ) as ListTag<CompoundTag>
            for (enchantment in this.enchantments) {
                val enchant = CompoundTag()
                enchant.putString("id", enchantment.enchantment.string)
                enchant.putInt("lvl", enchantment.level)
                enchantments.add(enchant)
            }
            nbt.put("Enchantments", enchantments)
            if (hideEnchants) nbt.put("HideFlags", IntTag(99))
            if (tags.size > 0) {
                val customTags = ListTag.createUnchecked(
                    CompoundTag::class.java
                ) as ListTag<CompoundTag>
                customTags.addAll(tags)
                nbt.put("custom", customTags)
            }
        }
        item.nbtData(nbt)
        return item
    }

    companion object {
        fun of(builder: ItemBuilder): ItemBuilder {
            val item: BaseItemStack = builder.item
            return ItemBuilder(item.itemType())
                .lore(item.lore<Any>().stream().map { e: Any? -> e as String? }.collect(Collectors.toList()))
                .enchantments(builder.enchantments.toTypedArray<GUIEnchantUtil>())
                .name(item.displayName())
                .amount(item.amount().toInt())
        }

        fun from(item: BaseItemStack): ItemBuilder {
            val builder = ItemBuilder(item.itemType())
            builder.name(item.displayName())
            builder.lore(item.lore())
            if (item.nbtData()["Enchantments"] != null) {
                val enchants = item.nbtData().getListTag("Enchantments") as ListTag<CompoundTag>
                for (tag in enchants) {
                    builder.enchantment(
                        GUIEnchantsList.valueOf(
                            tag.getString("id").replace("minecraft:", "").uppercase(Locale.getDefault())
                        ),
                        tag.getInt("lvl")
                    )
                }
            }
            return builder
        }
    }

    private fun replaceFormat(text: String): String { return ForceFormatCode.replaceFormat(text) }
}