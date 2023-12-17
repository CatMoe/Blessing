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

package catmoe.fallencrystal.moefilter.network.limbo.dimension.llbit

import catmoe.fallencrystal.moefilter.MoeFilterBungee
import catmoe.fallencrystal.moefilter.network.limbo.handler.LimboLoader
import catmoe.fallencrystal.moefilter.util.message.v2.MessageUtil
import catmoe.fallencrystal.translation.utils.version.Version
import com.github.benmanes.caffeine.cache.Caffeine
import se.llbit.nbt.CompoundTag
import se.llbit.nbt.Tag
import java.io.BufferedInputStream
import java.io.DataInputStream
import java.util.zip.GZIPInputStream
import kotlin.system.exitProcess

@Suppress("MemberVisibilityCanBePrivate")
object StaticDimension {

    val d1 = nbtReader("dim/d1.nbt")
    val d2 = nbtReader("dim/d2.nbt")

    val cacheDimension = Caffeine.newBuilder().build<Version, Tag>()

    var dim = LimboLoader.dimensionType

    fun init() {
        Version.entries.forEach { cacheDimension.put(it, dim.dimension.getFullCodec(it)) }
    }

    fun nbtReader(path: String): CompoundTag {
        return try {
            CompoundTag.read(DataInputStream(BufferedInputStream(GZIPInputStream(
                MoeFilterBungee.instance.javaClass.classLoader.getResourceAsStream(path)))))[""] as CompoundTag
        } catch (exception: Exception) {
            MessageUtil.logError("[MoeLimbo] A critical error when reading nbt files. Killing process..")
            exitProcess(404)
        }
    }

}