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

package net.miaomoe.blessing.nbt.exception

class EncodeTagException(
    override val cause: Throwable,
    override val message: String? = null
) : TagException(cause, message ?: "Failed to encode nbt tag.") {

    constructor(message: String): this(IllegalArgumentException(message))

    companion object {
        @JvmStatic
        fun create(exception: Throwable) = EncodeTagException(exception)
    }

}