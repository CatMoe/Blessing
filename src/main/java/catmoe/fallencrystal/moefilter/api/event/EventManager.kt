package catmoe.fallencrystal.moefilter.api.event

import catmoe.fallencrystal.moefilter.MoeFilter
import com.github.benmanes.caffeine.cache.Caffeine
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.plugin.Plugin
import java.lang.reflect.Method
import java.util.concurrent.CopyOnWriteArrayList

@Suppress("unused")
object EventManager {

    private val listeners: MutableList<EventListener> = CopyOnWriteArrayList()
    private val plugin = MoeFilter.instance as Plugin
    private val scheduler = ProxyServer.getInstance().scheduler
    private val listenerPlugin = Caffeine.newBuilder().build<EventListener, Plugin>()

    fun triggerEvent(event: MoeAsyncEvent) { scheduler.runAsync(plugin) { listeners.forEach { methodTrigger(it, event) } } }

    fun triggerEvent(event: MoeEvent) { listeners.forEach { methodTrigger(it, event) } }

    private fun methodTrigger(listener: EventListener, event: Any) {
        val methods = listener.javaClass.declaredMethods
        val lPlugin = listenerPlugin.getIfPresent(listener) ?: plugin
        if (event is MoeAsyncEvent) { methods.forEach { if (needSend(event, it)) { scheduler.runAsync(lPlugin) {  try { it.invoke(it, event) } catch (e: Exception) { e.printStackTrace() } } } } }
        if (event is MoeEvent) { methods.forEach { try { it.invoke(it, event) } catch (e: Exception) { e.printStackTrace() } } }
    }

    private fun needSend(event: Any, method: Method): Boolean { return method.isAnnotationPresent(FilterEvent::class.java) && method.parameterCount == 1 && event::class.java.isAssignableFrom(method.parameterTypes[0]) }

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
