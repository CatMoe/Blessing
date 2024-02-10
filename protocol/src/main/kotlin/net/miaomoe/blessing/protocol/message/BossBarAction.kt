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

interface BossBarAction<T> {

    val id: Int

    abstract class Add : BossBarAction<BossBar>
    abstract class Remove : BossBarAction<Unit>
    abstract class UpdateHealth : BossBarAction<Float>
    abstract class UpdateTitle : BossBarAction<MixedComponent>
    abstract class UpdateStyle : BossBarAction<Style>
    abstract class UpdateFlags : BossBarAction<Byte>

    companion object {
        @JvmStatic
        val ADD = object : Add() { override val id = 0 }
        @JvmStatic
        val REMOVE = object : Remove() { override val id = 1 }
        @JvmStatic
        val UPDATE_HEALTH = object : UpdateHealth() { override val id = 2 }
        @JvmStatic
        val UPDATE_TITLE = object : UpdateTitle() { override val id = 3 }
        @JvmStatic
        val UPDATE_STYLE = object : UpdateStyle() { override val id = 4 }
        @JvmStatic
        val UPDATE_FLAGS = object : UpdateFlags() { override val id = 5 }

        @JvmStatic
        val entries = mutableListOf(
            ADD,
            REMOVE,
            UPDATE_HEALTH,
            UPDATE_TITLE,
            UPDATE_STYLE,
            UPDATE_FLAGS
        )
    }

}