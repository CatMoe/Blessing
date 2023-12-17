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

package catmoe.fallencrystal.moefilter.network.limbo.util

import catmoe.fallencrystal.moefilter.network.limbo.compat.message.NbtMessage
import catmoe.fallencrystal.moefilter.network.limbo.handler.LimboHandler
import catmoe.fallencrystal.moefilter.network.limbo.packet.LimboS2CPacket
import catmoe.fallencrystal.moefilter.network.limbo.packet.s2c.*
import catmoe.fallencrystal.moefilter.network.limbo.packet.s2c.PacketTitle.Action
import catmoe.fallencrystal.translation.utils.version.Version
import net.kyori.adventure.text.Component

@Suppress("MemberVisibilityCanBePrivate")
class LimboMessage(val handler: LimboHandler) {

    data class Title(
        var title: NbtMessage = NbtMessage.EMPTY,
        var subTitle: NbtMessage = NbtMessage.EMPTY,
        var time: TitleTime = TitleTime()
    ) {
        constructor(title: Component, subTitle: Component) : this(
            NbtMessage.create(title), NbtMessage.create(subTitle)
        )

        data class TitleTime(
            var fadeIn: Int = 0,
            var stay: Int = 0,
            var fadeOut: Int = 0
        ) { val isInvalid get()= (fadeIn <= 0 && stay <=0 && fadeOut <=0) }
    }

    fun sendMessage(message: NbtMessage, flush: Boolean = false) =
        this.write(PacketServerChat(PacketServerChat.MessageType.CHAT, message), flush)

    fun sendMessage(message: Component, flush: Boolean = false) =
        this.sendMessage(NbtMessage.create(message), flush)

    fun sendTitle(title: Title, flush: Boolean = false) {
        val modern = handler.version.moreOrEqual(Version.V1_17)
        fun write(packet: LimboS2CPacket) = handler.writePacket(packet)
        if (title.time.isInvalid || (title.title.isEmpty && title.subTitle.isEmpty))
            this.writeResetTitle()
        else {
            title.title.takeIf { it.isNotEmpty }?.let { write(PacketTitle(Action.TITLE, it)) }
            title.subTitle.takeIf { it.isNotEmpty }?.let {
                write(if (modern) PacketSubTitle(it) else PacketTitle(Action.SUBTITLE, it))
            }
            title.time.let {
                write(if (modern) PacketTitleTime(it) else PacketTitle(Action.TIMES, time = it))
            }
        }
        if (flush) handler.channel.flush()
    }

    fun writeResetTitle(flush: Boolean = false) = this.write(
        if (handler.version.moreOrEqual(Version.V1_17))
            PacketTitle(Action.RESET)
        else
            PacketTitleReset(),
        flush
    )

    fun sendActionbar(message: NbtMessage, flush: Boolean = false) {
        val version = handler.version
        this.write(
            if (version.fromTo(Version.V1_11_1, Version.V1_16_4))
                PacketTitle(Action.ACTION_BAR, message)
            else
                PacketServerChat(PacketServerChat.MessageType.ACTION_BAR, message),
            flush
        )
    }

    fun sendActionbar(message: Component, flush: Boolean = false) =
        this.sendActionbar(NbtMessage.create(message), flush)

    private fun write(packet: LimboS2CPacket, flush: Boolean = false) =
        if (flush) handler.sendPacket(packet) else handler.writePacket(packet)
}