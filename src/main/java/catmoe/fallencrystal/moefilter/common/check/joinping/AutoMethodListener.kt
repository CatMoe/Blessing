package catmoe.fallencrystal.moefilter.common.check.joinping

import catmoe.fallencrystal.moefilter.api.event.EventListener
import catmoe.fallencrystal.moefilter.api.event.FilterEvent
import catmoe.fallencrystal.moefilter.api.event.events.AttackEndedEvent
import catmoe.fallencrystal.moefilter.api.event.events.AttackStartEvent
import catmoe.fallencrystal.moefilter.common.attack.AttackType

class AutoMethodListener : EventListener {
    @FilterEvent
    fun onAttackStart(event: AttackStartEvent) {
        val methods = event.method
        JoinPingChecks.setInAttack(true)
        if (methods.contains(AttackType.ONCE_JOIN) || methods.contains(AttackType.PING_AND_JOIN)) { setMethod(JoinPingType.JOIN_FIRST_REST); return }
        if (methods.contains(AttackType.REJOIN)) { setMethod(JoinPingType.PING_FIRST_REST) }
    }

    @FilterEvent
    fun onAttackEnded(event: AttackEndedEvent) { setMethod(JoinPingType.JOIN_FIRST_REST); JoinPingChecks.setInAttack(false) }

    private fun setMethod(event: JoinPingType) { JoinPingChecks.setAutoMethod(event) }
}