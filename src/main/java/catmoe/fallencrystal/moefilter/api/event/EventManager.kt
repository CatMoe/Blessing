package catmoe.fallencrystal.moefilter.api.event

import catmoe.fallencrystal.moefilter.util.plugin.FilterPlugin
import net.md_5.bungee.api.ProxyServer
import java.util.concurrent.CopyOnWriteArrayList

object EventManager {

    private val listeners: MutableList<EventListener> = CopyOnWriteArrayList()
    private val plugin = FilterPlugin.getPlugin()
    private val scheduler = ProxyServer.getInstance().scheduler

    fun triggerEvent(event: Any) {
        scheduler.runAsync(plugin) {
            if (listeners.isEmpty()) return@runAsync
            for (it in listeners) {
                val methods = it.javaClass.declaredMethods
                if (methods.isNullOrEmpty()) return@runAsync
                for (method in methods) {
                    if (method.isAnnotationPresent(FilterEvent::class.java) && method.parameterCount == 1 && event::class.java.isAssignableFrom(method.parameterTypes[0])) {
                        scheduler.runAsync(plugin) { try { method.invoke(it, event) } catch (e: Exception) { e.printStackTrace() } }
                    }
                }
            }
        }
    }

    fun registerListener(c: EventListener) { scheduler.runAsync(plugin) { listeners.add(c) } }

    fun unregisterListener(c: EventListener) {
        scheduler.runAsync(plugin) {
            val listenerToRemove = mutableListOf<EventListener>()
            listeners.forEach { if (it::class.java == c::class.java) { listenerToRemove.add(it) } }
            listeners.removeAll(listenerToRemove)
        }
    }

    fun listenerList(): MutableList<EventListener> { return listeners }
}
