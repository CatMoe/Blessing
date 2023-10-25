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

package catmoe.fallencrystal.moefilter.common.check.proxy

import java.net.InetAddress
import java.util.concurrent.CopyOnWriteArrayList

@Suppress("unused")
object ProxyChecker {
    private val apis: MutableCollection<IProxyChecker> = CopyOnWriteArrayList()

    fun addAPI(api: IProxyChecker) { apis.add(api); api.schedule() }

    fun removeAPI(api: IProxyChecker) { apis.forEach { if (api::class.java == it::class.java) { apis.remove(it); it.stopSchedule() } } }

    fun dropAll() { apis.forEach { it.stopSchedule(); apis.remove(it) } }

    fun check(address: InetAddress) { apis.forEach { it.addAddress(address) } }
}