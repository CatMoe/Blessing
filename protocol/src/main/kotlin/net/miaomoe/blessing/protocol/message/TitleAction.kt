/*
 * Copyright (C) 2023-2024. CatMoe / Blessing Contributors
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
package net.miaomoe.blessing.protocol.message

import net.miaomoe.blessing.nbt.chat.MixedComponent
import net.miaomoe.blessing.protocol.util.ByteMessage
import net.miaomoe.blessing.protocol.version.Version

@Suppress("unused")
interface TitleAction<T> {
    val id: Int
    fun write(
        value: T,
        byteBuf: ByteMessage,
        version: Version
    )

    abstract class NullTitleAction : TitleAction<Unit> {
        override val id = i++
        override fun write(
            value: Unit,
            byteBuf: ByteMessage,
            version: Version
        ) {
            // Not Impl.
        }
    }

    abstract class ComponentAction : TitleAction<MixedComponent> {
        override val id = i++
        override fun write(
            value: MixedComponent,
            byteBuf: ByteMessage,
            version: Version
        ) {
            byteBuf.writeChat(value, version)
        }
    }

    abstract class TimeAction : TitleAction<TitleTime> {
        override val id = i++
        override fun write(
            value: TitleTime,
            byteBuf: ByteMessage,
            version: Version
        ) {
            byteBuf.writeInt(value.fadeIn)
            byteBuf.writeInt(value.stay)
            byteBuf.writeInt(value.fadeOut)
        }
    }

    companion object {

        private var i = 0

        @JvmStatic
        val TITLE: ComponentAction = object : ComponentAction() {}
        @JvmStatic
        val SUBTITLE: ComponentAction = object : ComponentAction() {}
        @JvmStatic
        val ACTION_BAR: ComponentAction = object : ComponentAction() {}
        @JvmStatic
        val TIMES: TimeAction = object : TimeAction() {}
        @JvmStatic
        val CLEAR: NullTitleAction = object : NullTitleAction() {}
        @JvmStatic
        val RESET: NullTitleAction = object : NullTitleAction() {}
    }
}
