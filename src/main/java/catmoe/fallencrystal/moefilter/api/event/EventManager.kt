package catmoe.fallencrystal.moefilter.api.event

import catmoe.fallencrystal.moefilter.MoeFilter
import net.md_5.bungee.api.ProxyServer

object EventManager {
    private val listeners: MutableList<EventListener> = ArrayList()

    fun triggerEvent(event: Any) {
        ProxyServer.getInstance().scheduler.runAsync(MoeFilter()) {
            if (listeners.isEmpty()) return@runAsync
            for (it in listeners) {
                for (method in it.javaClass.declaredMethods) {
                    if (method.isAnnotationPresent(FilterEvent().javaClass) && method.parameterCount == 1 && event::class.java.isAssignableFrom(method.parameterTypes[0])) {
                        try { method.invoke(it, event) } catch (e: Exception) { e.printStackTrace() }
                    }
                }
            }
        }
    }

    fun registerListener(c: EventListener) {
        if (listeners.contains(c)) throw ConcurrentModificationException("$c is already registered")
        listeners.add(c)
    }

    fun unregisterListener(c: EventListener) {
        if (!listeners.contains(c)) throw NullPointerException("$c haven't register listener!")
        listeners.remove(c)
    }

    fun listenerList(): MutableList<EventListener> { return listeners }
}
