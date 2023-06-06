package catmoe.fallencrystal.moefilter.common.utils.counter

import catmoe.fallencrystal.moefilter.api.event.EventListener
import catmoe.fallencrystal.moefilter.api.event.FilterEvent
import catmoe.fallencrystal.moefilter.api.event.events.AttackEndedEvent
import catmoe.fallencrystal.moefilter.api.event.events.AttackStartEvent
import catmoe.fallencrystal.moefilter.util.message.notification.Notifications

class SessionCounterListener : EventListener {
    private var inAttack = false
    @FilterEvent
    fun startSessionCount(event: AttackStartEvent) { if (!inAttack) { inAttack=true; ConnectionCounter.setInAttack(true); Notifications.onAddAutoNotificationPlayer() } }
    @FilterEvent
    fun stopSessionCount(event: AttackEndedEvent) { if (inAttack) { inAttack=false; ConnectionCounter.setInAttack(false); Notifications.onInvalidateAutoNotificationPlayer() } }
}