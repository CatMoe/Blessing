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
import net.kyori.adventure.nbt.StringBinaryTag
import net.miaomoe.blessing.nbt.NbtUtil.toNamed
import net.miaomoe.blessing.nbt.TagProvider
import net.miaomoe.blessing.nbt.damage.DamageTags.Util.copyAndAdd
import net.miaomoe.blessing.nbt.damage.DamageValues.DeathMessageType.FALL_VARIANTS
import net.miaomoe.blessing.nbt.damage.DamageValues.DeathMessageType.INTENTIONAL_GAME_DESIGN
import net.miaomoe.blessing.nbt.damage.DamageValues.Effects.*
import net.miaomoe.blessing.nbt.damage.DamageValues.Scaling.ALWAYS

@Suppress("MemberVisibilityCanBePrivate", "SpellCheckingInspection")
enum class DamageTags(val values: MutableList<DamageValues>) : TagProvider {
    V1_19(mutableListOf(
        DamageValues("arrow", 0),
        DamageValues("bad_respawn_point", 1, scaling = ALWAYS , deathMessageType = INTENTIONAL_GAME_DESIGN),
        DamageValues("cactus", 2),
        DamageValues("cramming", 3, 0.0F),
        DamageValues("dragon_breath", 4, 0.0F),
        DamageValues("drown", 5, 0.0F, effects = DROWNING),
        DamageValues("dry_out", 6, messageId = "dryout"),
        DamageValues("explosion", 7, scaling = ALWAYS),
        DamageValues("fall", 8, 0.0F, deathMessageType = FALL_VARIANTS),
        DamageValues("falling_anvil", 9, messageId = "anvil"),
        DamageValues("falling_block", 10),
        DamageValues("falling_stalactite", 11),
        DamageValues("fireball", 12, effects = BURNING),
        DamageValues("fireworks", 13),
        DamageValues("fly_into_wall", 14, 0.0F),
        DamageValues("freeze", 15, 0.0F, effects = FREEZING),
        DamageValues("generic", 16, 0.0F),
        DamageValues("hot_floor", 17, effects = BURNING),
        DamageValues("in_fire", 18, effects = BURNING),
        DamageValues("in_wall", 19, 0.0F),
        DamageValues("indirect_magic", 20, 0.0F),
        DamageValues("lava", 21, effects = BURNING),
        DamageValues("lightning_bolt", 22,),
        DamageValues("magic", 23, 0.0F),
        DamageValues("mob_attack", 24, messageId = "mob"),
        DamageValues("mob_attack_no_aggro", 25, messageId = "mob"),
        DamageValues("mob_projectile", 26, messageId = "mob"),
        DamageValues("on_fire", 27, 0.0F, effects = BURNING),
        DamageValues("out_of_world", 28, 0.0F),
        DamageValues("player_attack", 29, messageId = "player"),
        DamageValues("player_explosion", 30, scaling = ALWAYS, messageId = "explosion.player"),
        DamageValues("sonic_boom", 31, 0.0F, ALWAYS, messageId = "sonic_boom"),
        DamageValues("stalagmite", 32, 0.0F),
        DamageValues("starve", 33, 0.0F),
        DamageValues("sting", 34),
        DamageValues("sweet_berry_bush", 35, effects = POKING),
        DamageValues("throns", 36, effects = THORNS),
        DamageValues("thrown", 37),
        DamageValues("trident", 38),
        DamageValues("unattributed_fireball", 39, messageId = "onFire", effects = BURNING),
        DamageValues("wither", 40, 0.0F),
        DamageValues("wither_skull", 41)
    )),
    V1_20(V1_19.values.copyAndAdd(
        DamageValues("outside_border", 42, 0.0F, ALWAYS, "badRespawnPoint"),
        DamageValues("generic_kill", 43, 0.0F, ALWAYS, "badRespawnPoint"))
    );

    internal object Util {

        internal fun <T> List<T>.copyAndAdd(vararg new: T): MutableList<T> {
            val copy = this.toMutableList()
            copy.addAll(new)
            return copy
        }
    }

    override fun toTag(): BinaryTag {
        val values = CompoundBinaryTag.builder()
        for (value in this.values) { values.put(value.toTag() as CompoundBinaryTag) }
        return CompoundBinaryTag
            .builder()
            .put("type", StringBinaryTag.stringBinaryTag("minecraft:damage_type"))
            .put("value", values.build())
            .build()
            .toNamed()
    }

}