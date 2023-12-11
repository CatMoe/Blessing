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
import dev.simplix.protocolize.api.Protocolize
import dev.simplix.protocolize.api.inventory.Inventory
import dev.simplix.protocolize.api.inventory.InventoryClick
import dev.simplix.protocolize.api.inventory.InventoryClose
import dev.simplix.protocolize.api.item.ItemStack
import dev.simplix.protocolize.data.inventory.InventoryType
import net.kyori.adventure.text.Component
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.connection.ProxiedPlayer
import java.util.concurrent.CopyOnWriteArrayList

@Suppress("unused", "EmptyMethod")
abstract class MenuBuilder {
    private var type: InventoryType = InventoryType.GENERIC_9X3
    private var player: ProxiedPlayer? = null
    private val items = HashMap<Int, ItemStack>()
    private val emptyItems: MutableList<ItemStack> = CopyOnWriteArrayList()
    private var title: Component? = null
    private var inv: Inventory? = null

    private val protocolize get() = player?.let { Protocolize.playerProvider().player(it.uniqueId) }

     open fun type(type: InventoryType) { this.type=type }

    open fun setPlayer(player: ProxiedPlayer) { this.player=player }

    open fun define() {
        // idk
    }

    open fun setTitle(component: Component) { this.title=component }

    open fun build(): Inventory {
        define()
        val inv = Inventory(type)
        updateItem(inv)
        this.inv=inv
        return inv
    }

    private fun updateItem(inv: Inventory) {
        val titleBaseComponent = if (title != null) { ComponentUtil.toBaseComponents(title!!) ?: TextComponent() } else TextComponent()
        titleBaseComponent.isItalic=false
        inv.title(titleBaseComponent.toLegacyText())
        if (emptyItems.isNotEmpty()) { inv.items() }
        items.keys.forEach { inv.item(it, items[it]) }
    }

    open fun open() {
        val inv = this.inv ?: build()
        inv.onClick { click: InventoryClick -> onClick(click) }
        inv.onClose { close: InventoryClose -> onClose(close) }
        protocolize!!.openInventory(inv)
    }

    open fun onClose(close: InventoryClose) {
        // Override to listen when closing inventory
    }

    open fun onClick(click: InventoryClick) {
        // Override to listen when clicking inventory
    }

    open fun close() = protocolize?.closeInventory()

    open fun update() {
        val inv = this.inv ?: return
        updateItem(inv)
        protocolize?.openInventory(inv)
    }

    protected fun setItem(index: Int, item: ItemStack) { items[index]=item }

    protected fun removeItem(index: Int) { items.remove(index) }

    fun clear() { items.clear() }

}