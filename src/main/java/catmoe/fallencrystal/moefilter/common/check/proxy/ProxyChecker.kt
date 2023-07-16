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

package catmoe.fallencrystal.moefilter.common.check.proxy

import java.net.InetAddress
import java.util.concurrent.CopyOnWriteArrayList

object ProxyChecker {
    private val apis: MutableCollection<IProxyChecker> = CopyOnWriteArrayList()

    fun addAPI(api: IProxyChecker) { apis.add(api); api.schedule() }

    fun removeAPI(api: IProxyChecker) { apis.forEach { if (api::class.java == it::class.java) { apis.remove(it); it.stopSchedule() } } }

    fun dropAll() { apis.forEach { it.stopSchedule(); apis.remove(it) } }

    fun check(address: InetAddress) { apis.forEach { it.addAddress(address) } }
}