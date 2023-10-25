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

package catmoe.fallencrystal.moefilter.common.firewall.lockdown

import com.github.benmanes.caffeine.cache.Caffeine
import net.md_5.bungee.api.ProxyServer
import java.net.InetAddress
import java.net.InetSocketAddress
import java.util.concurrent.atomic.AtomicBoolean

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