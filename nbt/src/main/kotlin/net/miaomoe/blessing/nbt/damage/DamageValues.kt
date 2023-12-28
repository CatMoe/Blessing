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

package net.miaomoe.blessing.nbt.damage

import net.kyori.adventure.nbt.BinaryTag
import net.kyori.adventure.nbt.CompoundBinaryTag
import net.miaomoe.blessing.nbt.NbtUtil.put
import net.miaomoe.blessing.nbt.TagProvider
import net.miaomoe.blessing.nbt.dimension.NbtVersion

data class DamageValues(
    val name: String,
    val id: Int,
    val exhaustion: Float = 0.1F,
    val scaling: Scaling = Scaling.NON_PLAYER,
    val messageId: String = defaultMessageId(name),
    val deathMessageType: DeathMessageType? = null,
    val effects: Effects? = null
) : TagProvider {

    enum class Scaling(val string: String) {
        NON_PLAYER("when_caused_by_living_non_player"),
        ALWAYS("always");
    }

    enum class DeathMessageType(val string: String) {
        INTENTIONAL_GAME_DESIGN("intentional_game_design"),
        FALL_VARIANTS("fall_variants")
    }

    enum class Effects(val string: String) {
        DROWNING("drowning"),
        BURNING("burning"),
        FREEZING("freezing"),
        POKING("poking"),
        THORNS("thorns")
    }

    override fun toTag(version: NbtVersion?): BinaryTag {
        val compound = CompoundBinaryTag.builder()
            .put("name", "minecraft:$name")
            .put("id", id)
        val element = CompoundBinaryTag.builder()
        effects?.let { element.put("effects", it.string) }
        element
            .put("scaling", scaling.string)
            .put("exhaustion", exhaustion)
            .put("message_id", messageId)
        deathMessageType?.let { element.put("death_message_type", it.string) }
        return compound.put("element", element.build()).build()
    }

    companion object {
        private fun defaultMessageId(name: String): String {
            return if (name.contains("_")) {
                val parts = name.split("_")
                val result = StringBuilder(parts[0])
                for (i in 1 until parts.size) {
                    if (parts[i].isNotEmpty()) {
                        result.append(parts[i][0].uppercase())
                        if (parts[i].length > 1) {
                            result.append(parts[i].substring(1))
                        }
                    }
                }
                result.toString()
            } else name
        }
    }
}