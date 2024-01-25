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

import net.miaomoe.blessing.protocol.packet.type.PacketToClient
import net.miaomoe.blessing.protocol.util.ByteMessage
import net.miaomoe.blessing.protocol.util.LazyInit
import net.miaomoe.blessing.protocol.version.Version
import net.miaomoe.blessing.protocol.version.VersionRange

@Suppress("MemberVisibilityCanBePrivate")
class PacketCacheGroup @JvmOverloads constructor(
    val packet: PacketToClient,
    val description: String? = null,
    val copySame: Boolean = false,
    initVersions: VersionRange? = null
) {

    private val map = LazyInit<MutableMap<Version, PacketCache>> { mutableMapOf() }
    private val cached = LazyInit<List<PacketCache>> { mutableListOf() }

    init {
        initVersions?.let(::cache)
    }

    private val mapOrNull get() = map.valueDirectly


    fun invalidateAt(version: Version) {
        mapOrNull?.remove(version)
    }

    fun invalidateAt(version: VersionRange) {
        mapOrNull?.let { version.forEach(it::remove) }
    }

    fun invalidate() {
        mapOrNull?.clear()
    }

    fun cache(version: Version) {
        cacheAndGet(version)
    }

    fun cacheAndGet(version: Version): PacketCache {
        val bytes = ByteMessage.create().use {
            packet.encode(it, version)
            it.toByteArray()
        }.takeUnless { it.isEmpty() }
        val mapOrNull = this.mapOrNull
        val cached = if (copySame && mapOrNull != null) {
            mapOrNull.let {
                it.values.firstOrNull { cache -> bytes?.contentEquals(cache.byteArray) ?: (cache.byteArray == null) }
                    ?: PacketCache(packet::class, bytes, description)
            }
        } else PacketCache(packet::class, bytes, description)
        map.value[version] = cached
        return cached
    }

    fun cache(version: VersionRange) = version.forEach(::cache)

    fun setAt(version: Version, cache: PacketCache) {
        require(cache.kClass == this.packet::class) { "PacketClass must be equal!" }
        map.value[version] = cache
    }

    fun setAt(version: VersionRange, cache: PacketCache) = version.forEach { setAt(it, cache) }

    fun copyTo(original: Version, target: Version) {
        val map = mapOrNull ?: return
        map[original]?.let { map[target] = it }
    }

    fun copyTo(original: Version, target: VersionRange) {
        val map = mapOrNull ?: return
        val value = map[original] ?: return
        target.forEach { map[it] = value }
    }

    fun getIfCached(version: Version) = mapOrNull?.get(version)

    fun getNonNull(version: Version) = mapOrNull?.get(version) ?: cacheAndGet(version)

}