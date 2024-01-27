/*
 * Copyright (C) 2023-2023. CatMoe / Blessing Contributors
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

package net.miaomoe.blessing.nbt.chat

import com.google.gson.JsonParser
import net.kyori.adventure.nbt.BinaryTag
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.miaomoe.blessing.nbt.NbtUtil
import org.jetbrains.annotations.ApiStatus.Experimental

@Suppress("MemberVisibilityCanBePrivate")
class MixedComponent(
    val json: String,
    val tag: BinaryTag = NbtUtil.serialize(JsonParser.parseString(json))
) {

    constructor(component: Component) : this(component.toJson())

    @Experimental
    constructor(nbt: BinaryTag) : this(NbtUtil.deserialize(nbt).toString(), nbt)

    fun toComponent() = GsonComponentSerializer.gson().deserialize(json)

    companion object {

        private val miniMessage = MiniMessage.miniMessage()

        val EMPTY = MixedComponent(miniMessage.deserialize(""))

        private fun Component.toJson() = GsonComponentSerializer.gson().serialize(this)
    }

    override fun toString() = "MixedComponent(json=$json, tag=$tag)"

}