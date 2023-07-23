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

package catmoe.fallencrystal.moefilter.common.firewall.lockdown

import com.github.benmanes.caffeine.cache.Caffeine
import net.md_5.bungee.api.ProxyServer
import java.net.InetAddress
import java.net.InetSocketAddress
import java.util.concurrent.atomic.AtomicBoolean

@Suppress("unused")
object LockdownManager {
    val whitelistCache = Caffeine.newBuilder().build<InetAddress, Boolean>()
    val state = AtomicBoolean(false)

    fun setLockdown(active: Boolean) {
        when (active) {
            true -> { activeLockdown(); state.set(true) }
            false -> { state.set(false); whitelistCache.invalidateAll() }
        }
    }

    fun verify(address: InetAddress): Boolean { return if (state.get()) whitelistCache.getIfPresent(address) == true else true }

    private fun activeLockdown() {
        ProxyServer.getInstance().players.forEach { whitelistCache.put(((it.socketAddress as InetSocketAddress).address), true) }
    }
}