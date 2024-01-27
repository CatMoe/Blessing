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

package net.miaomoe.blessing.nbt.chat

import net.kyori.adventure.nbt.BinaryTag
import net.kyori.adventure.nbt.BinaryTagTypes
import net.kyori.adventure.nbt.CompoundBinaryTag
import net.kyori.adventure.nbt.CompoundBinaryTag.Builder
import net.kyori.adventure.nbt.ListBinaryTag
import net.miaomoe.blessing.nbt.NbtUtil.put
import net.miaomoe.blessing.nbt.NbtUtil.toListTag
import net.miaomoe.blessing.nbt.NbtUtil.toNamed
import net.miaomoe.blessing.nbt.NbtUtil.toNbt
import net.miaomoe.blessing.nbt.TagProvider
import net.miaomoe.blessing.nbt.dimension.NbtVersion

object ChatRegistry : TagProvider {
    private val defaultParameter = listOf("sender", "content")
    override fun toTag(version: NbtVersion?): BinaryTag {
        require(version != null) { "NbtVersion cannot be null!" }
       val newer = version.moreOrEqual(NbtVersion.V1_19_1)
       val systemElement = if (newer) {
           buildElement("system", defaultParameter)
       } else {
           buildElement(CompoundBinaryTag.empty(), "system".toNbt().toNamed("priority"))
       }
        val gameElement = CompoundBinaryTag
            .builder()
            .put("overlay", CompoundBinaryTag.empty())
            .let { if (newer) buildElement("game_info", builder = it) else it.build() }
        return CompoundBinaryTag
            .builder()
            .put("type", "minecraft:chat_type")
            .put("value", listOf(
                buildRoot("system", 1, systemElement),
                buildRoot("game_info", 2, gameElement)
            ).toListTag())
            .build()
    }

    private fun buildRoot(name: String, id: Int, element: CompoundBinaryTag) =
        CompoundBinaryTag
            .builder()
            .put("name", "minecraft:$name")
            .put("id", id)
            .put("element", element)
            .build()

    private fun buildElement(
        chat: String,
        parameters: List<String> = listOf(),
        builder: Builder = CompoundBinaryTag.builder()
    ) =
        buildElement(createChatTag(chat, parameters), createChatTag("$chat.narrate", parameters), builder)

    private fun buildElement(
        chat: CompoundBinaryTag,
        narration: CompoundBinaryTag,
        builder: Builder = CompoundBinaryTag.builder()
    ) =
        builder
            .put("chat", chat)
            .put("narration", narration)
            .build()

    private fun createChatTag(key: String, parameters: List<String>): CompoundBinaryTag {
        return CompoundBinaryTag
            .builder()
            .put("translation_key", "chat.type.$key")
            .put("parameters", ListBinaryTag.listBinaryTag(BinaryTagTypes.STRING, parameters.map { it.toNbt() }))
            .build()
    }
}