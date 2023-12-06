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

package catmoe.fallencrystal.moefilter.network.limbo.packet.s2c

import catmoe.fallencrystal.moefilter.network.common.ByteMessage
import catmoe.fallencrystal.moefilter.network.limbo.packet.LimboS2CPacket
import catmoe.fallencrystal.translation.utils.version.Version

@Suppress("MemberVisibilityCanBePrivate")
class PacketGameEvent(
    var events: Events = Events.START_WAITING_LEVEL_CHUNKS,
    var subId: Float = 0F // 0 = Default
) : LimboS2CPacket() {

    override fun encode(packet: ByteMessage, version: Version?) {
        packet.writeByte(events.id)
        packet.writeFloat(subId)
    }

    // https://wiki.vg/Protocol#Game_Event
    enum class Events(val id: Int) {
        NO_RESPAWN_BLOCK_AVAILABLE(0),
        BEGIN_RAINING(1),
        END_RAINING(2),
        CHANGE_GAME_MODE(3),
        WIN_GAME(4),
        DEMO_EVENT(5),
        ARROW_HIT_PLAYER(6),
        RAIN_LEVEL_CHANGE(7),
        THUNDER_LEVEL_CHANGE(8),
        PLAY_PUFFER_FISH_STING_SOUND(9),
        PLAY_ELDER_GUARDIAN_APPEARANCE(10),
        ENABLE_RESPAWN_SCREEN(11),
        LIMIT_CRAFTING(12),
        START_WAITING_LEVEL_CHUNKS(13);
    }
}