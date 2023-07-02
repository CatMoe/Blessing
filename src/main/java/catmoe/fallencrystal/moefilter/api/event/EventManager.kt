package catmoe.fallencrystal.moefilter.api.event

import catmoe.fallencrystal.moefilter.MoeFilter
import com.github.benmanes.caffeine.cache.Caffeine
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.plugin.Plugin
import java.util.concurrent.CopyOnWriteArrayList

@Suppress("unused")
object EventManager {

    private val listeners: MutableList<EventListener> = CopyOnWriteArrayList()
    private val plugin = MoeFilter.instance as Plugin
    private val scheduler = ProxyServer.getInstance().scheduler
    private val listenerPlugin = Caffeine.newBuilder().build<EventListener, Plugin>()

    fun triggerEvent(event: MoeEvent) {
        scheduler.runAsync(plugin) {
            if (listeners.isEmpty()) return@runAsync
            for (it in listeners) {
                val methods = it.javaClass.declaredMethods
                val asyncPlugin = listenerPlugin.getIfPresent(it) ?: plugin
                if (methods.isNullOrEmpty()) return@runAsync
                for (method in methods) {
                    if (method.isAnnotationPresent(FilterEvent::class.java) && method.parameterCount == 1 && event::class.java.isAssignableFrom(method.parameterTypes[0])) {
                        scheduler.runAsync(asyncPlugin) { try { method.invoke(it, event) } catch (e: Exception) { e.printStackTrace() } }
                    }
                }
            }
        }
    }

    @Deprecated("Use method registerListener(Plugin, EventListener)")
    fun registerListener(c: EventListener) { listeners.add(c) }

    fun registerListener(p: Plugin, c: EventListener) { listenerPlugin.put(c, p); listeners.add(c) }

    fun unregisterListener(c: EventListener) {
        scheduler.runAsync(plugin) {
            val listenerToRemove = mutableListOf<EventListener>()
            listeners.forEach { if (it::class.java == c::class.java) { listenerToRemove.add(it); listenerPlugin.invalidate(it) } }
            listeners.removeAll(listenerToRemove)
        }
    }

    fun listenerList(): MutableList<EventListener> { return listeners }
}
