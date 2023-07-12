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

package catmoe.fallencrystal.moefilter.common.check.misc

import catmoe.fallencrystal.moefilter.common.check.AbstractCheck
import catmoe.fallencrystal.moefilter.common.check.info.CheckInfo
import catmoe.fallencrystal.moefilter.common.check.info.impl.AddressCheck
import catmoe.fallencrystal.moefilter.common.config.LocalConfig
import catmoe.fallencrystal.moefilter.util.message.v2.MessageUtil
import com.github.benmanes.caffeine.cache.Caffeine

class DomainCheck : AbstractCheck() {

   private val cache = Caffeine.newBuilder().build<String, Boolean>()
   private var enable = false
   private var debug = false

   init { instance=this; init() }
   
   override fun increase(info: CheckInfo): Boolean {
      if (!enable) return false
      val address = (info as AddressCheck).address
      val host = info.virtualHost!!.hostString
      if (host == address.address.toString().replace("/", "")) return false
      if (debug) { MessageUtil.logInfo("[MoeFilter] [AntiBot] [DomainCheck] ${info.address.address} try to connect from $host") }
      return cache.getIfPresent(host.lowercase()) == null
   }

   fun init() {
      cache.invalidateAll()
      val config = LocalConfig.getConfig().getConfig("domain-check")
      enable = config.getBoolean("enabled")
      debug = LocalConfig.getConfig().getBoolean("debug")
      config.getStringList("allow-lists").forEach { cache.put(it.lowercase(), true) }
   }

   companion object {
      lateinit var instance: DomainCheck
          private set
   }
}
