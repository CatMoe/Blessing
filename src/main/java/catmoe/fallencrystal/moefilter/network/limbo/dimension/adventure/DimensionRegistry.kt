/*
 * Copyright 2023. CatMoe / FallenCrystal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package catmoe.fallencrystal.moefilter.network.limbo.dimension.adventure

import catmoe.fallencrystal.moefilter.MoeFilter
import catmoe.fallencrystal.moefilter.network.limbo.handler.MoeLimbo
import net.kyori.adventure.nbt.CompoundBinaryTag
import net.kyori.adventure.nbt.TagStringIO
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.stream.Collectors


@Suppress("SpellCheckingInspection")
object DimensionRegistry {

    var codec_Legacy = readFromFile("dim/d0.snbt")
    var codec_1_16 = readFromFile("dim/d1.snbt")
    var codec_1_18_2 = readFromFile("dim/d2.snbt")
    var codec_1_19 = readFromFile("dim/d3.snbt")
    var codec_1_19_1 = readFromFile("dim/d4.snbt")
    var codec_1_19_4 = readFromFile("dim/d5.snbt")
    var codec_1_20 = readFromFile("dim/d6.snbt")

    private fun readString(i: String): CompoundBinaryTag { return TagStringIO.get().asCompound(i) }

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
                MoeFilter.instance.getResourceAsStream(file) ?: throw NullPointerException("Unknown file path or instance getResourceAsStream is not valid")
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