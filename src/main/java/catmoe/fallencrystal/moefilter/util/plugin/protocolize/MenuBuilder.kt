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

@Suppress("unused")
abstract class MenuBuilder {
    private var type: InventoryType = InventoryType.GENERIC_9X3
    private var player: ProxiedPlayer? = null
    private val items = HashMap<Int, ItemStack>()
    private val emptyItems: MutableList<ItemStack> = CopyOnWriteArrayList()
    private var title: Component? = null
    private var inv: Inventory? = null

     open fun type(type: InventoryType) { this.type=type }

    open fun setPlayer(player: ProxiedPlayer) { this.player=player }

    open fun define() {}

    open fun setTitle(component: Component) { this.title=component }

    open fun build(): Inventory {
        define()
        val inv = Inventory(type)
        val titleBaseComponent = if (title != null) { ComponentUtil.toBaseComponents(title!!) } else TextComponent()
        titleBaseComponent.isItalic=false
        inv.title(titleBaseComponent.toLegacyText())
        if (emptyItems.isNotEmpty()) { inv.items() }
        items.keys.forEach { inv.item(it, items[it]) }
        this.inv=inv
        return inv
    }

    open fun open() {
        val inv = this.inv ?: build()
        inv.onClick { click: InventoryClick -> onClick(click) }
        inv.onClose { close: InventoryClose -> onClose(close) }
        Protocolize.playerProvider().player(player!!.uniqueId).openInventory(inv)
    }

    open fun onClose(close: InventoryClose) {}

    open fun onClick(click: InventoryClick) {}

    open fun close() { Protocolize.playerProvider().player(player!!.uniqueId).closeInventory() }

    open fun update() { build(); Protocolize.playerProvider().player(player!!.uniqueId).openInventory(inv) }

    protected fun setItem(index: Int, item: ItemStack) { items[index]=item }

    protected fun removeItem(index: Int) { items.remove(index) }

    fun clear() { items.clear() }

}