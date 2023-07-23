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

package catmoe.fallencrystal.moefilter.common.state

import catmoe.fallencrystal.moefilter.api.event.EventManager
import catmoe.fallencrystal.moefilter.api.event.events.AttackStoppedEvent
import catmoe.fallencrystal.moefilter.api.event.events.UnderAttackEvent
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean

@Suppress("unused", "MemberVisibilityCanBePrivate")
object StateManager {

    val inAttack = AtomicBoolean(false)
    var attackMethods: MutableCollection<AttackState> = CopyOnWriteArrayList()
    val duration = AttackDuration()

    fun setAttackMethod(method: Collection<AttackState>) {
        if (!inAttack.get()) return
        attackMethods.clear(); attackMethods.addAll(method)
        EventManager.triggerEvent(UnderAttackEvent(attackMethods))
    }

    fun fireAttackEvent() {
        if (attackMethods.isEmpty()) {
            EventManager.triggerEvent(UnderAttackEvent(listOf(AttackState.NOT_HANDLED)))
        } else {
            EventManager.triggerEvent(UnderAttackEvent(attackMethods))
        }
        inAttack.set(true)
        if (inAttack.get()) { duration.start() }
    }

    fun fireNotInAttackEvent() { EventManager.triggerEvent(AttackStoppedEvent()); attackMethods.clear(); inAttack.set(false); duration.stop() }

}