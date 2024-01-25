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

package net.miaomoe.blessing.protocol.packet.configuration

import net.kyori.adventure.nbt.BinaryTag
import net.miaomoe.blessing.nbt.dimension.Dimension
import net.miaomoe.blessing.nbt.dimension.World
import net.miaomoe.blessing.protocol.packet.type.PacketToClient
import net.miaomoe.blessing.protocol.util.ByteMessage
import net.miaomoe.blessing.protocol.version.Version

@Suppress("MemberVisibilityCanBePrivate")
class PacketRegistryData(
    var tag: BinaryTag? = null
) : PacketToClient {

    private var dimension: Dimension = World.OVERWORLD.dimension

    constructor(world: World) : this(world.dimension)

    constructor(dimension: Dimension) : this(null) {
        this.dimension=dimension
    }

    override fun encode(byteBuf: ByteMessage, version: Version) =
        byteBuf.writeNamelessTag(tag ?: dimension.toTag(version.toNbtVersion()))

}