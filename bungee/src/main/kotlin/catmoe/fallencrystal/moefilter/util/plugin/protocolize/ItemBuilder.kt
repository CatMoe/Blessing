/*
 * Copyright (C) 2023-2023. CatMoe / MoeFilter Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package catmoe.fallencrystal.moefilter.util.plugin.protocolize

import catmoe.fallencrystal.translation.utils.component.ComponentUtil
import dev.simplix.protocolize.api.chat.ChatElement
import dev.simplix.protocolize.api.item.ItemStack
import dev.simplix.protocolize.data.ItemType
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.TextComponent
import net.querz.nbt.tag.CompoundTag
import net.querz.nbt.tag.IntTag
import net.querz.nbt.tag.ListTag

@Suppress("unused", "MemberVisibilityCanBePrivate")
class ItemBuilder(material: ItemType) {
    private val item = ItemStack(material)
    private val enchantments: MutableList<Enchantments> = ArrayList()
    private var hideEnchant = false
    private var tags: MutableList<CompoundTag> = ArrayList()

    fun type(material: ItemType): ItemBuilder { item.itemType(material); return this }

    fun amount(amount: Int): ItemBuilder { item.amount(amount.toByte()); return this }

    fun Component.toChatElement(): ChatElement<BaseComponent> =
        ChatElement.of(ComponentUtil.toBaseComponents(this) ?: TextComponent())

    fun name(name: Component): ItemBuilder {
        name.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)
        item.displayName(name.toChatElement())
        return this
    }

    fun lore(lore: Component): ItemBuilder {
        lore.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)
        item.addToLore(lore.toChatElement())
        return this
    }

    fun lore(lore: List<Component>): ItemBuilder { lore.forEach { lore(it) }; return this }

    fun enchantment(enchantment: Enchantments): ItemBuilder { this.enchantments.add(enchantment); return this }

    fun clearEnchantment(): ItemBuilder { this.enchantments.clear(); return this }

    fun hideEnchantment(hide: Boolean): ItemBuilder { this.hideEnchant=hide; return this }

    fun getEnchantments(): List<Enchantments> { return enchantments }

    @Suppress("UNCHECKED_CAST")
    fun build(): ItemStack {
        val nbt = item.nbtData()
        if (enchantments.isNotEmpty()) {
            val enchantments = ListTag.createUnchecked(CompoundTag::class.java) as ListTag<CompoundTag>
            for (enchantment in this.enchantments) {
                val enchant = CompoundTag()
                enchant.putString("id", enchantment.string)
                enchant.putInt("lvl", enchantment.level)
                enchantments.add(enchant)
            }
            nbt.put("Enchantments", enchantments)
            if (hideEnchant) nbt.put("HideFlags", IntTag(5))
            if (tags.isNotEmpty()) {
                val customTags = (ListTag.createUnchecked(CompoundTag::class.java) as ListTag<CompoundTag>)
                customTags.addAll(tags)
                nbt.put("custom", customTags)
            }
        }
        item.nbtData(nbt)
        return item
    }
}