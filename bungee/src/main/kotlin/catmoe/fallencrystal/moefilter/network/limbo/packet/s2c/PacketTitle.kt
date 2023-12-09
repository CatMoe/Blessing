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
import catmoe.fallencrystal.moefilter.network.limbo.compat.message.NbtMessage
import catmoe.fallencrystal.moefilter.network.limbo.packet.LimboS2CPacket
import catmoe.fallencrystal.moefilter.network.limbo.util.LimboMessage.Title.TitleTime
import catmoe.fallencrystal.translation.utils.version.Version

@Suppress("MemberVisibilityCanBePrivate")
class PacketTitle(
    var action: Action = Action.TITLE,
    var message: NbtMessage = NbtMessage.EMPTY,
    var time: TitleTime = TitleTime()
) : LimboS2CPacket() {
    override fun encode(byteBuf: ByteMessage, version: Version?) {
        if (version!!.moreOrEqual(Version.V1_17)) {
            require(action == Action.TITLE) {
                "The Action must be TITLE! (for 1.17+ clients)"
            }
            message.write(byteBuf, version)
            return
        }

        val action = this.action
        var index = action.ordinal
        if (version.lessOrEqual(Version.V1_10) && index >= 2) index--

        byteBuf.writeVarInt(index)

        when (action) {
            Action.TIMES -> {
                byteBuf.writeInt(time.fadeIn)
                byteBuf.writeInt(time.stay)
                byteBuf.writeInt(time.fadeOut)
            }
            Action.RESET -> {}
            else -> message.write(byteBuf, version)
        }
    }

    enum class Action {
        TITLE,
        SUBTITLE,
        ACTION_BAR,
        TIMES,
        CLEAR,
        RESET
    }

}