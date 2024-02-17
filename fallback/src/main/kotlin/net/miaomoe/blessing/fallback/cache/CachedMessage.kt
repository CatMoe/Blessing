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

package net.miaomoe.blessing.fallback.cache

import net.miaomoe.blessing.nbt.chat.MixedComponent
import net.miaomoe.blessing.protocol.packet.play.PacketServerChat
import net.miaomoe.blessing.protocol.util.ByteMessage
import net.miaomoe.blessing.protocol.version.Version
import net.miaomoe.blessing.protocol.version.VersionRange

@Suppress("MemberVisibilityCanBePrivate")
class CachedMessage(val message: MixedComponent) {

    val group = PacketCacheGroup(
        PacketServerChat(message),
        "Cached message for $message",
        true
    )

    val legacy = create(false)
    val nbt = create(true)

    init {
        group.setAt(VersionRange.of(Version.V1_7_2, Version.V1_20), legacy)
        group.setAt(VersionRange.of(Version.V1_20_2, Version.V1_20_3), nbt)
    }

    fun create(nbt: Boolean) = PacketCache(
        PacketServerChat::class,
        ByteMessage.create().use {  byteBuf ->
            byteBuf.writeChat(
                message,
                if (nbt) Version.max else Version.min
            )
            byteBuf.toByteArray()
        },
        group.description
    )

}