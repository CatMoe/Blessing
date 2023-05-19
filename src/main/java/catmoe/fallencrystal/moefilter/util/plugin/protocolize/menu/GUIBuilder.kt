package catmoe.fallencrystal.moefilter.util.plugin.protocolize.menu

import dev.simplix.protocolize.api.Protocolize
import dev.simplix.protocolize.api.inventory.Inventory
import dev.simplix.protocolize.api.inventory.InventoryClick
import dev.simplix.protocolize.api.inventory.InventoryClose
import dev.simplix.protocolize.api.item.ItemStack
import dev.simplix.protocolize.data.ItemType
import dev.simplix.protocolize.data.inventory.InventoryType
import net.md_5.bungee.api.connection.ProxiedPlayer

abstract class GUIBuilder {
    private var type: InventoryType? = null
    var player: ProxiedPlayer? = null
    private val items = HashMap<Int, ItemStack>()
    private val emptyItems: MutableList<ItemStack> = ArrayList()
    private var title: String? = null
    fun type(type: InventoryType?) {
        this.type = type
    }

    fun type(): InventoryType? {
        return type
    }

    open fun define(p: ProxiedPlayer?) {
        player = p
    }

    fun setTitle(string: String?) {
        title = string
    }

    private fun build(): Inventory {
        val inv = Inventory(type)
        inv.title(title)
        if (emptyItems.isNotEmpty()) {
            inv.items()
        }
        for (index in items.keys) {
            val item = items[index]
            inv.item(index, item)
        }
        return inv
    }

    open fun open(player: ProxiedPlayer) {
        val i = build()
        i.onClick { click: InventoryClick? -> onClick(click) }
        i.onClose { close: InventoryClose? -> onClose(close) }
        val Protocolplayer = Protocolize.playerProvider().player(player.uniqueId)
        Protocolplayer.openInventory(i)
    }

    fun updateItems() {
        val protocolizePlayer = Protocolize.playerProvider().player(player!!.uniqueId)
        val inv = protocolizePlayer.proxyInventory()
        inv.clear()
        for (index in items.keys) {
            val item = items[index]
            inv.item(index, item)
        }
        inv.update()
    }

    fun update() {
        clear()
        define(player)
    }

    fun getInventoryType(value: Int): InventoryType {
        return when (value) {
            0 -> InventoryType.GENERIC_3X3
            1 -> InventoryType.GENERIC_9X1
            2 -> InventoryType.GENERIC_9X2
            3 -> InventoryType.GENERIC_9X3
            4 -> InventoryType.GENERIC_9X4
            5 -> InventoryType.GENERIC_9X5
            6 -> InventoryType.GENERIC_9X6
            else -> InventoryType.GENERIC_9X3
        }
    }

    fun getSlot(slot: Int): ItemStack? {
        return items[slot]
    }

    open fun onClose(close: InventoryClose?) {}
    open fun onClick(click: InventoryClick?) {}
    fun close() {
        val protocolizePlayer = Protocolize.playerProvider().player(player!!.uniqueId)
        protocolizePlayer.closeInventory()
    }

    protected fun setEmpty(itemtype: ItemType?) {
        val item = ItemBuilder(itemtype).amount(1).name("").build()
        val totalSlots = type!!.getTypicalSize(player!!.pendingConnection.version)
        for (i in 0 until totalSlots) {
            emptyItems.add(item)
        }
    }

    protected fun setItem(index: Int, itemBuilder: ItemStack?) {
        if (itemBuilder != null) {
            items[index] = itemBuilder
        }
    }

    protected fun removeItem(index: Int) {
        items.remove(index)
    }

    fun clear() {
        items.clear()
    }
}