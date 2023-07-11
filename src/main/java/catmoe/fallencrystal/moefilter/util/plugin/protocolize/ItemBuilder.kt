/*
 * Copyright 2023. CatMoe / FallenCrystal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package catmoe.fallencrystal.moefilter.util.plugin.protocolize

import catmoe.fallencrystal.moefilter.util.message.component.ComponentUtil
import dev.simplix.protocolize.api.item.ItemStack
import dev.simplix.protocolize.data.ItemType
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import net.querz.nbt.tag.CompoundTag
import net.querz.nbt.tag.IntTag
import net.querz.nbt.tag.ListTag

@Suppress("unused")
class ItemBuilder(material: ItemType) {
    private val item = ItemStack(material)
    private val enchantments: MutableList<Enchantments> = ArrayList()
    private var hideEnchant = false
    private var tags: MutableList<CompoundTag> = ArrayList()

    fun type(material: ItemType): ItemBuilder { item.itemType(material); return this }

    fun amount(amount: Int): ItemBuilder { item.amount(amount.toByte()); return this }

    fun name(name: Component): ItemBuilder {
        name.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)
        val bc = ComponentUtil.toBaseComponents(name)
        bc.isItalic=false
        item.displayName(bc.toLegacyText())
        return this
    }

    fun lore(lore: Component): ItemBuilder {
        lore.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)
        val bc = ComponentUtil.toBaseComponents(lore)
        bc.isItalic=false
        item.addToLore(bc.toLegacyText())
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
            if (tags.size > 0) {
                val customTags = (ListTag.createUnchecked(CompoundTag::class.java) as ListTag<CompoundTag>)
                customTags.addAll(tags)
                nbt.put("custom", customTags)
            }
        }
        item.nbtData(nbt)
        return item
    }
}