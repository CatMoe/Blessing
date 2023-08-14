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

package catmoe.fallencrystal.moefilter.network.limbo.dimension.llbit

import catmoe.fallencrystal.moefilter.MoeFilter
import catmoe.fallencrystal.moefilter.network.limbo.handler.MoeLimbo
import catmoe.fallencrystal.moefilter.network.limbo.util.Version
import catmoe.fallencrystal.moefilter.util.message.v2.MessageUtil
import com.github.benmanes.caffeine.cache.Caffeine
import se.llbit.nbt.CompoundTag
import se.llbit.nbt.Tag
import java.io.BufferedInputStream
import java.io.DataInputStream
import java.util.zip.GZIPInputStream
import kotlin.system.exitProcess

@Suppress("MemberVisibilityCanBePrivate")
object StaticDimension {

    val d1 = nbtReader("resource/dim/d1.nbt")
    val d2 = nbtReader("resource/dim/d2.nbt")

    val cacheDimension = Caffeine.newBuilder().build<Version, Tag>()

    var dim = MoeLimbo.dimensionType.llbit

    fun init() {
        Version.values().forEach { cacheDimension.put(it, dim.dimension.getFullCodec(it)) }
    }

    fun nbtReader(path: String): CompoundTag {
        return try {
            CompoundTag.read(DataInputStream(BufferedInputStream(GZIPInputStream(
                MoeFilter.instance.javaClass.classLoader.getResourceAsStream(path)))))[""] as CompoundTag
        } catch (exception: Exception) {
            MessageUtil.logError("[MoeLimbo] A critical error when reading nbt files. Killing process..")
            exitProcess(404)
        }
    }

}