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

import net.kyori.adventure.title.Title
import net.miaomoe.blessing.nbt.chat.MixedComponent

data class Title(
    var title: MixedComponent = MixedComponent.EMPTY,
    var subTitle: MixedComponent = MixedComponent.EMPTY,
    var times: TitleTime
) {

    companion object {
        @JvmStatic
        fun fromAdventure(title: Title)
        = Title(
            MixedComponent(title.title()),
            MixedComponent(title.subtitle()),
            title.times()?.let(TitleTime::fromAdventure) ?: TitleTime.zero
        )
    }

}