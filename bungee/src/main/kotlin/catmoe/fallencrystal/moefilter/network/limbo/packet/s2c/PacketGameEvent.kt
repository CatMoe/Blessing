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
        packet.writeByte(events.ordinal)
        packet.writeFloat(subId)
    }

    // https://wiki.vg/Protocol#Game_Event
    enum class Events {
        NO_RESPAWN_BLOCK_AVAILABLE,
        BEGIN_RAINING,
        END_RAINING,
        CHANGE_GAME_MODE,
        WIN_GAME,
        DEMO_EVENT,
        ARROW_HIT_PLAYER,
        RAIN_LEVEL_CHANGE,
        THUNDER_LEVEL_CHANGE,
        PLAY_PUFFER_FISH_STING_SOUND,
        PLAY_ELDER_GUARDIAN_APPEARANCE,
        ENABLE_RESPAWN_SCREEN,
        LIMIT_CRAFTING,
        START_WAITING_LEVEL_CHUNKS;
    }
}