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

package catmoe.fallencrystal.moefilter.network.limbo.dimension.adventure

import catmoe.fallencrystal.moefilter.MoeFilterBungee
import catmoe.fallencrystal.moefilter.network.limbo.handler.MoeLimbo
import net.kyori.adventure.nbt.CompoundBinaryTag
import net.kyori.adventure.nbt.TagStringIO
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.stream.Collectors


@Suppress("SpellCheckingInspection")
object DimensionRegistry {

    var codec_Legacy = readFromFile("dim/d1.snbt")
    var codec_1_16 = readFromFile("dim/d0.snbt")
    var codec_1_18_2 = readFromFile("dim/d2.snbt")
    var codec_1_19 = readFromFile("dim/d3.snbt")
    var codec_1_19_1 = readFromFile("dim/d4.snbt")
    var codec_1_19_4 = readFromFile("dim/d5.snbt")
    var codec_1_20 = readFromFile("dim/d6.snbt")

/*
    fun test() {
        val dimensions: ListBinaryTag = tag.getCompound("minecraft:dimension_type").getList("value")

        val overWorld = (dimensions[0] as CompoundBinaryTag)["element"] as CompoundBinaryTag?
        val nether = (dimensions[2] as CompoundBinaryTag)["element"] as CompoundBinaryTag?
        val theEnd = (dimensions[3] as CompoundBinaryTag)["element"] as CompoundBinaryTag?
    }

 */

    private fun readFromFile(file: String): CompoundBinaryTag {
        val reader = InputStreamReader((
                MoeFilterBungee.instance.getResourceAsStream(file) ?: throw NullPointerException("Unknown file path or instance getResourceAsStream is not valid")
                ), StandardCharsets.UTF_8)
        val bufReader = BufferedReader(reader)
        val content = bufReader.lines().collect(Collectors.joining("\n"))
        reader.close()
        bufReader.close()
        return TagStringIO.get().asCompound(content)
    }

    fun getDimension(type: DimensionType, tag: CompoundBinaryTag): Dimension {
        val dimension =
            (tag.getCompound("minecraft:dimension_type").getList("value")[type.tagId] as CompoundBinaryTag)["element"] as CompoundBinaryTag
        return Dimension(type.dimensionId, type.dimensionName, dimension)
    }

    var defaultDimension1_16 : Dimension = getDimension(MoeLimbo.dimensionType.adventure, codec_1_16)
    var defaultDimension1_18_2 : Dimension = getDimension(MoeLimbo.dimensionType.adventure, codec_1_18_2)
}