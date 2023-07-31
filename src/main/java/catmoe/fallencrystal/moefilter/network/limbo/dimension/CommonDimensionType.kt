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

package catmoe.fallencrystal.moefilter.network.limbo.dimension

@Suppress("unused", "SpellCheckingInspection")
enum class CommonDimensionType(
    @JvmField val adventure: catmoe.fallencrystal.moefilter.network.limbo.dimension.adventure.DimensionType,
    @JvmField val llbit: catmoe.fallencrystal.moefilter.network.limbo.dimension.llbit.DimensionType
) {
    OVERWORLD(
        catmoe.fallencrystal.moefilter.network.limbo.dimension.adventure.DimensionType.OVERWORLD,
        catmoe.fallencrystal.moefilter.network.limbo.dimension.llbit.DimensionType.OVERWORLD
    ),
    NETHER(
        catmoe.fallencrystal.moefilter.network.limbo.dimension.adventure.DimensionType.THE_NETHER,
        catmoe.fallencrystal.moefilter.network.limbo.dimension.llbit.DimensionType.NETHER
    ),
    THE_END(
        catmoe.fallencrystal.moefilter.network.limbo.dimension.adventure.DimensionType.THE_END,
        catmoe.fallencrystal.moefilter.network.limbo.dimension.llbit.DimensionType.THE_END
    ),
}