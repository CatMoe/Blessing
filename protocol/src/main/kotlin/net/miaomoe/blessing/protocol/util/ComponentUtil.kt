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

package net.miaomoe.blessing.protocol.util

import com.google.gson.JsonElement
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.miaomoe.blessing.nbt.chat.MixedComponent

@Suppress("MemberVisibilityCanBePrivate")
object ComponentUtil {

    val legacy = LegacyComponentSerializer.legacySection()
    val gson = GsonComponentSerializer.gson()
    val miniMessage = MiniMessage.miniMessage()

    fun Component.toLegacyText() = legacy.serialize(this)

    fun Component.toLegacyComponent() = legacy.deserialize(this.toLegacyText())

    fun String.fromLegacyText() = legacy.deserialize(this)

    fun Component.toJsonElement() = gson.serializeToTree(this)

    fun Component.toJson() = gson.serialize(this)

    fun String.fromJson() = gson.deserialize(this)

    fun JsonElement.fromJsonElement() = gson.deserializeFromTree(this)

    fun Component.toMixedComponent() = MixedComponent(this)

    fun MixedComponent.toComponentFromJson() = this.json.fromJson()

    fun String.toComponent() = miniMessage.deserialize(this)

    fun String.toComponent(legacy: Boolean) = this.toComponent().let { if (legacy) it.toLegacyComponent() else it }

    fun String.toMixedComponent() = this.toComponent().toMixedComponent()

    fun List<String>.toComponent() = this.joinToString("<reset><newline>").toComponent()

    fun List<String>.toMixedComponent() = this.toComponent().toMixedComponent()

}