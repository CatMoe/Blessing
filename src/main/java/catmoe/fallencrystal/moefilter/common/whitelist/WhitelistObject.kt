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

package catmoe.fallencrystal.moefilter.common.whitelist

import com.github.benmanes.caffeine.cache.Caffeine
import java.net.InetAddress

object WhitelistObject {
    private val cache = Caffeine.newBuilder().build<InetAddress, Boolean>()
    private val ips = mutableListOf<InetAddress>()

    fun isWhitelist(address: InetAddress): Boolean {  return cache.getIfPresent(address) ?: false }

    fun setWhitelist(address: InetAddress, type: WhitelistType) {
        when (type) {
            WhitelistType.ADD -> { addWhitelist(address) }
            WhitelistType.REMOVE -> { removeWhitelist(address) }
        }
    }

    fun getAllWhitelist(): List<InetAddress> { return ips }

    private fun addWhitelist(address: InetAddress) {
        if (cache.getIfPresent(address) != null && ips.contains(address)) return
        cache.put(address, true)
        ips.add(address)
    }

    private fun removeWhitelist(address: InetAddress) {
        if (cache.getIfPresent(address) == null && !ips.contains(address)) return
        cache.invalidate(address)
        ips.remove(address)
    }
}